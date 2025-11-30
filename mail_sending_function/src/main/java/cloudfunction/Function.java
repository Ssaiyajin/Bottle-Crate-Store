package cloudfunction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths; // <--- added
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class Function implements BackgroundFunction<GcsEvent> { // renamed generic
    private static final Logger logger = Logger.getLogger(Function.class.getName());

    @Override
    public void accept(GcsEvent event, Context context) { // updated param type
        if (event == null) {
            logger.severe("Received null event");
            return;
        }

        String bucket = event.bucket;
        String fileName = event.name;
        String mail = null;
        if (event.metadata != null) {
            mail = event.metadata.get("email");
            if (mail == null) {
                mail = event.metadata.get("Email");
            }
        }

        if (bucket == null || fileName == null) {
            logger.severe("Missing bucket or file name in event: bucket=" + bucket + " name=" + fileName);
            return;
        }

        if (mail == null || mail.isEmpty()) {
            logger.severe("No recipient email found in object metadata; aborting send for " + fileName);
            return;
        }

        Path tmpPath = Paths.get("/tmp", fileName);
        try {
            downloadObjectToTemp(bucket, fileName, tmpPath);
            Session session = initializeMailing();
            Message message = generateMessage(session, tmpPath.toFile(), mail);
            Transport.send(message);
            logger.info("Email sent to " + mail + " with attachment " + fileName);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to send email for object " + fileName, e);
        } finally {
            try {
                Files.deleteIfExists(tmpPath);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to delete temp file " + tmpPath, e);
            }
        }
    }

    private void downloadObjectToTemp(String bucket, String fileName, Path target) throws IOException {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        // prefer single-call get(bucket, name)
        var blob = storage.get(bucket, fileName);
        if (blob == null) {
            throw new IOException("Cloud Storage object not found: " + bucket + "/" + fileName);
        }
        Files.createDirectories(target.getParent());
        blob.downloadTo(target);
    }

    private Message generateMessage(Session session, File attachment, String mailTo) throws MessagingException, IOException {
        Message message = new MimeMessage(session);

        String mailUser = System.getenv("MAIL_USER");
        if (mailUser == null || mailUser.isEmpty()) {
            mailUser = "no-reply@example.com";
        }

        message.setFrom(new InternetAddress(mailUser));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));
        message.setSubject("Demanded Beverage Information");

        String msg = "Hi,<br/><br/>attached you find the Beverage Information you demanded. Have fun with it :)<br/><br/>Kind regards,<br/>DSAM Group 15 :)";

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        attachmentBodyPart.attachFile(attachment);
        multipart.addBodyPart(attachmentBodyPart);

        message.setContent(multipart);
        return message;
    }

    private Session initializeMailing() {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        final String user = System.getenv("MAIL_USER");
        final String pass = System.getenv("MAIL_PASS");

        if (user == null || pass == null) {
            logger.warning("MAIL_USER or MAIL_PASS not set in environment variables. Email sending will likely fail.");
        }

        return Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });
    }

    private void sendOrStoreMail(String recipient, String subject, String body) {
        FirebaseInit fb = FirebaseInit.getInstance();
        if (fb.isAvailable()) {
            // existing code to send / store via Firebase or cloud mail API
            // ...existing code...
        } else {
            // fallback: write the email as a local JSON or .eml file for later processing
            try {
                Path out = fb.getLocalRoot().resolve("outgoing_mail");
                Files.createDirectories(out);
                String filename = "mail_" + Instant.now().toEpochMilli() + ".json";
                String json = "{ \"to\": \"" + recipient + "\", \"subject\": \"" + subject.replace("\"","'") + "\", \"body\": \"" + body.replace("\"","'") + "\" }";
                Files.write(out.resolve(filename), json.getBytes("UTF-8"), StandardOpenOption.CREATE_NEW);
            } catch (Exception ex) {
                // log and swallow - we're in fallback mode
            }
        }
    }
}

// replace the old package-level Event with a renamed type to avoid duplicate/visibility problems
class GcsEvent {
    public String bucket;
    public String name;
    public Map<String, String> metadata;
}