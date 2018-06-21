import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class RequestHandler implements HttpHandler {


    private static int STATUS_OK = 200;

    @Override
    // Only use local variable to be thread safe
    public void handle(HttpExchange httpExchange) throws IOException {
        String clientAddr = httpExchange.getRemoteAddress().getHostString();
        System.out.println("\nHandle request for " + clientAddr);

        if (!httpExchange.getProtocol().equalsIgnoreCase("HTTP/1.1")){
            System.err.println("Unsupported protocol");
            httpExchange.close();
            return;
        }

        // Get Keep-alive header
        boolean keepAlive = false;
        Headers headers = httpExchange.getRequestHeaders();
        if (headers.containsKey("Connection")) {
            for (String value : headers.get("Connection")) {
                if (value.equalsIgnoreCase("Keep-Alive")) {
                    keepAlive = true;
                }
            }
        }

        System.out.println("Persistent connection: " + keepAlive);

        // Get URI
        // TODO
        /*URI uri = httpExchange.getRequestURI();
        String response = "Hello, World! You requested " + uri.getPath() + "\n";
        httpExchange.sendResponseHeaders(STATUS_OK, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());*/

        // Get Method
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

        // Close connection if there is no Keep-Alive header
        if (!keepAlive) {
            System.out.println("Closing connection of " + clientAddr);
            httpExchange.close();
        }
    }


    private void handleGetMethod(HttpExchange httpExchange) {
        // TODO: send content
    }

    private void handlePostMethod(HttpExchange httpExchange) throws IOException {
        // TODO: parse URL to get params
    }
}
