import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.OutputStream;
import java.io.IOException;

public class RequestHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("Handle request");

        if (!httpExchange.getProtocol().equalsIgnoreCase("HTTP/1.1")){
            System.err.println("Unsupported protocol");
            httpExchange.close();
            return;
        }

        // TODO: get
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
