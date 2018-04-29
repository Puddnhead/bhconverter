package bhc.util;

import java.util.Optional;

/**
 * Utility methods
 *
 * Created by MVW on 4/15/2018.
 */
public class SystemUtils {

    public static void logError(String errorMessage, Optional<Throwable> throwable) {
        System.err.println("ERROR:" + errorMessage);
        throwable.ifPresent(th -> System.err.println(th.getMessage()));
    }
}
