public class Main {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        // dynamically defines the number of threads
        int max_thread = Runtime.getRuntime().availableProcessors();

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException ignored) {}

            if (port < 1 || port > 65535) {
                System.out.println("Port must be between 1 and 65535. Use default port (" + DEFAULT_PORT + ")");
                port = DEFAULT_PORT;
            }
        }
        else {
            System.out.println("No port specify. Use default port (" + DEFAULT_PORT + ")");
        }

    }
}
