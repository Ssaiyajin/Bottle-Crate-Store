package cloudfunction;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FirebaseInit {
    private static FirebaseInit instance;
    private final boolean available;
    private final Firestore firestore;
    private final Storage storage;
    private final Path localRoot;

    private FirebaseInit() {
        Firestore f = null;
        Storage s = null;
        boolean ok = false;
        Path root = Paths.get(System.getProperty("java.io.tmpdir"), "bcs_local");
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            // ignore
        }
        try {
            f = FirestoreOptions.getDefaultInstance().getService();
            s = StorageOptions.getDefaultInstance().getService();
            ok = true;
        } catch (Throwable t) {
            ok = false;
        }
        firestore = f;
        storage = s;
        available = ok;
        localRoot = root;
    }

    public static synchronized FirebaseInit getInstance() {
        if (instance == null) instance = new FirebaseInit();
        return instance;
    }

    public boolean isAvailable() {
        return available;
    }

    public Firestore getFirestore() {
        return firestore;
    }

    public Storage getStorage() {
        return storage;
    }

    public Path getLocalRoot() {
        return localRoot;
    }
}