package cloudfunction;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FirebaseInit {
    private static final FirebaseInit INSTANCE = new FirebaseInit();
    private FirebaseInit() {}
    public static FirebaseInit getInstance() { return INSTANCE; }
    public boolean isAvailable() { return false; } // adjust if real Firebase is available
    public Path getLocalRoot() { return Paths.get(System.getProperty("java.io.tmpdir")); }
}