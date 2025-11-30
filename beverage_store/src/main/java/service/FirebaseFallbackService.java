package service;

import java.nio.file.Path;

/**
 * Minimal stub for compilation. Add the real method signatures used by ShoppingCartServiceImpl.
 */
public interface FirebaseFallbackService {
    // methods required by ShoppingCartServiceImpl
    boolean isAvailable();
    Path getLocalRoot();

    // add methods used by ShoppingCartServiceImpl, for example:
    // void someMethod(...);
}