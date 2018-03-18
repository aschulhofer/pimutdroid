package at.woodstick.mysampleapplication.util;

/**
 *
 */

public final class DummyUtil {

    public static final String DEFAULT_STRING_VALUE = "default";

    private DummyUtil() {
    }

    public static <T> T echo(T value) {
        return value;
    }

    public static int add(int a, int b) {
        return a + b;
    }

    public static int sbutract(int a, int b) {
        return a - b;
    }

    public static String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    public static String defaultIfNull(String value) {
        ValueProvider valueProvider = new ValueProvider(value, DEFAULT_STRING_VALUE);
        return valueProvider.defaultIfNull();
    }

    public static boolean isNullOrEmpty(String value) {
        if(value == null || value.isEmpty()) {
            return true;
        }
        else {
            return false;
        }
    }

    static class ValueProvider {
        private final String value;
        private final String defaultValue;

        public ValueProvider(String value, String defaultValue) {
            this.value = value;
            this.defaultValue = defaultValue;
        }

        public String defaultIfNull() {
            return value == null ? defaultValue : value;
        }
    }
}

