import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        HttpServer httpServer = null;
        ExecutorService executor = null;

        // dynamically defines the number of threads for the threads pool
        int max_thread = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(max_thread);

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

        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            System.err.println("Unable to create Http server on port " + port);
            e.printStackTrace();
            System.exit(1);
        }

        // ensure that httpServer has been created
        assert (httpServer != null);
        httpServer.createContext("/", new RequestHandler());
        httpServer.setExecutor(executor);
        httpServer.start();

        System.out.println("Server listening on port " + port + "...");

    }
}
