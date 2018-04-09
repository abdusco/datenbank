public class Logger {
    static void log(String message) {
        System.out.println(message);
    }

    public static void logError(String message) {
        System.out.println(String.format("ERROR: %s", message));
    }
}
