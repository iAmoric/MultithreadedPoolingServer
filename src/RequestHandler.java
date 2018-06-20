import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class RequestHandler implements HttpHandler {

    private String response = "Hello, World! from the server\n";
    private static int STATUS_OK = 200;
    private boolean keepAlive = false;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("Handle request");

        if (!httpExchange.getProtocol().equalsIgnoreCase("HTTP/1.1")){
            System.err.println("Unsupported protocol");
            httpExchange.close();
            return;
        }

        // Get Keep-alive header
        Headers headers = httpExchange.getRequestHeaders();
        for (String key : headers.keySet()) {
            if (key.equalsIgnoreCase("Connection")) {
                for (String value : headers.get(key)) {
                    if (value.equalsIgnoreCase("Keep-Alive")) {
                        keepAlive = true;
                    }
                }
            }
        }
        System.out.println("Persistent connection: " + keepAlive);

        String method = httpExchange.getRequestMethod();

        switch (method) {
            case "GET":
                handleGetMethod(httpExchange);
                break;
            case "POST":
                handlePostMethod(httpExchange);
                break;
            default:
                System.err.println("\tUnsupported Http method: " + method);
                break;
        }
    }


    private void handleGetMethod(HttpExchange httpExchange) {
        // TODO: send content
    }

    private void handlePostMethod(HttpExchange httpExchange) throws IOException {
        // TODO: parse URL to get params
    }
}
