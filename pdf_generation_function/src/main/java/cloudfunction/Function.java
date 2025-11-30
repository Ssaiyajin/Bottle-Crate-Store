package cloudfunction;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.lowagie.text.DocumentException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import com.google.cloud.firestore.Firestore;

public class Function implements HttpFunction {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEFAULT_PROJECT_ID = "beverage-store-group15";
    private static final String DEFAULT_BUCKET = "pdf_order";

    private final Firestore db;
    private final String projectId;

    private static final Logger log = LoggerFactory.getLogger(Function.class);

    public Function() {
        this.projectId = determineProjectId();
        this.db = initializeDB(projectId);
    }

    private static String determineProjectId() {
        String pid = System.getenv("GCP_PROJECT");
        if (pid == null || pid.isBlank()) {
            pid = System.getenv("GOOGLE_CLOUD_PROJECT");
        }
        return (pid == null || pid.isBlank()) ? DEFAULT_PROJECT_ID : pid;
    }

    /**
     * Initialize Firebase / Firestore without embedding or loading credentials here.
     * Rely on the runtime's default credentials (ADC). When running on GCP Cloud Functions
     * ADC is provided automatically; locally use application-default credentials via gcloud.
     */
    private static Firestore initializeDB(String projectId) {
        try {
            FirebaseOptions.Builder builder = FirebaseOptions.builder().setProjectId(projectId);
            synchronized (Function.class) {
                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(builder.build());
                }
            }
            return FirestoreClient.getFirestore();
        } catch (Exception e) {
            log.error("Failed to initialize Firestore/FirebaseApp", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        try {
            Order receivedOrder = parseOrder(request);
            log.info("Received order: {}", receivedOrder);

            if (receivedOrder == null || receivedOrder.getId() == null) {
                response.setStatusCode(400);
                response.getWriter().write("Invalid order payload: missing id");
                return;
            }

            String pdfFileName = "Order_Number_" + receivedOrder.getId() + "_Receipt.pdf";
            String generatedHtml = parseThymeTemplate(receivedOrder);
            byte[] pdf = convertHtmlToPdf(generatedHtml);

            if (pdf == null || pdf.length == 0) {
                log.error("PDF generation returned empty output");
                response.setStatusCode(500);
                response.getWriter().write("Failed to generate PDF");
                return;
            }

            storePdf(pdf, pdfFileName);
            storeUsageToFirestore(receivedOrder, db);

            response.setStatusCode(200);
            response.getWriter().write("Order processed: PDF stored and usage saved");
        } catch (IOException e) {
            log.error("I/O error while processing request", e);
            response.setStatusCode(500);
            response.getWriter().write("Server I/O error");
        } catch (Exception e) {
            log.error("Unhandled error", e);
            response.setStatusCode(500);
            response.getWriter().write("Server error");
        }
    }

    private Order parseOrder(HttpRequest request) throws IOException {
        try (InputStream in = request.getInputStream()) {
            return OBJECT_MAPPER.readValue(in, Order.class);
        } catch (IOException e) {
            log.error("Failed to parse order JSON", e);
            throw e;
        }
    }

    public String parseThymeTemplate(Order receivedOrder) {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false);
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        Context context = new Context();
        context.setVariable("order", receivedOrder);
        // Template name should exist on classpath (resources). Adjust name as needed.
        return templateEngine.process("shamim", context);
    }

    public byte[] convertHtmlToPdf(String html) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            log.info("PDF created successfully, size={} bytes", outputStream.size());
            return outputStream.toByteArray();
        } catch (DocumentException e) {
            log.error("PDF creation failed", e);
            throw new IOException("PDF creation failed", e);
        }
    }

    private void storePdf(byte[] pdfBytes, String filename) throws Exception {
        FirebaseInit fb = FirebaseInit.getInstance();
        if (fb.isAvailable()) {
            // existing Firebase storage logic (keep your bucket name / upload code)
            // Example placeholder: use fb.getStorage() to upload to bucket
            // ...existing code...
        } else {
            // fallback: write PDF locally on device (temp folder)
            Path out = fb.getLocalRoot().resolve("pdfs");
            Files.createDirectories(out);
            Path target = out.resolve(filename);
            Files.write(target, pdfBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            // Optionally log or return the local path so caller can attach/send it later
        }
    }

    private void storeUsageToFirestore(Order order, Firestore db) {
        if (order == null || db == null) {
            log.warn("Skipping Firestore write: order or db null");
            return;
        }
        try {
            DocumentReference docRef = db.collection("Orders").document(String.valueOf(order.getId()));
            Map<String, Object> data = new HashMap<>();
            data.put("Delivery_PLZ", order.getPostalCode());
            data.put("TimeStamp_Of_Order", order.getTimeStamp());
            for (int i = 0; i < order.getListOfItems().size(); i++) {
                OrderItem oi = order.getListOfItems().get(i);
                if (oi == null) continue;
                data.put("beverageName_" + i, oi.getBeverageName());
                data.put("beverageID_" + i, oi.getBeverageId());
                data.put("beverageQuantity_" + i, oi.getQuantity());
            }
            ApiFuture<WriteResult> result = docRef.set(data);
            log.info("Firestore write initiated for order id={}, updateTime={}", order.getId(), result.get());
        } catch (Exception e) {
            log.error("Failed to write usage to Firestore", e);
        }
    }
}