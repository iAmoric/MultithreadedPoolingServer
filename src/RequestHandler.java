import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler implements HttpHandler {


    private static int STATUS_OK = 200;
    private static int STATUS_NOT_FOUND = 404;
    private static String WEB_ROOT_DIRECTORY = System.getProperty("user.dir") + "/web";

    @Override
    // Only use local variable to be thread safe
    public void handle(HttpExchange httpExchange) throws IOException {
        String clientAddr = httpExchange.getRemoteAddress().getHostString();
        System.out.println("\nHandle request for " + clientAddr);

        // Only support HTTP/1.1 protocol
        if (!httpExchange.getProtocol().equalsIgnoreCase("HTTP/1.1")){
            System.err.println("Unsupported protocol");
            httpExchange.close();
            return;
        }

        // Get Keep-alive header
        boolean keepAlive = getKeepAlive(httpExchange);


        // Get Query parameterss
        String paramString = httpExchange.getRequestURI().getRawQuery();
        if (paramString != null) {
            System.out.println(paramString);
            Map<String, String> paramsMap = getParams(paramString);
            System.out.println("Query parameters:");
            for (Map.Entry<String, String> e : paramsMap.entrySet()) {
                System.out.println("\t" + e.getKey() + ": " + e.getValue());
            }
        }

        // Get Method
        String method = httpExchange.getRequestMethod();
        System.out.println(method);
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

    /**
     *
     * @param httpExchange
     * @throws IOException
     */
    private synchronized void handleGetMethod(HttpExchange httpExchange) throws IOException{
        // Get requested path
        String path = httpExchange.getRequestURI().getPath();
        OutputStream os = httpExchange.getResponseBody();

        // Get file for the response
        File file = getFile(path);
        if (file == null) {
            String response = path + " : Page not found\n";
            httpExchange.sendResponseHeaders(STATUS_NOT_FOUND, response.length());
            os.write(response.getBytes());
        }
        else {
            // Add content-type to the response header
            Headers header = httpExchange.getResponseHeaders();
            header.add("Content-type", "text/html");

            // Read content of the file
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            byte[] b  = new byte[(int)file.length()];
            bis.read(b, 0, b.length);

            // Send content to the client
            httpExchange.sendResponseHeaders(STATUS_OK, file.length());
            os.write(b,0, b.length);
        }
        os.flush();
        os.close();
    }

    private synchronized void handlePostMethod(HttpExchange httpExchange) throws IOException {
        // TODO: parse URL to get params
    }

    /**
     * Return a Map of every parameters in the request paramsString
     * @param paramsString
     * @return paramsMap
     */
    private synchronized Map<String, String> getParams(String paramsString) {
        String paramDelimiter = "&";
        String valueDelimiter = "=";

        Map<String, String> paramsMap = new HashMap<>();

        for (String params : paramsString.split(paramDelimiter)) {
            String[] values = params.split(valueDelimiter);
            if (values.length == 2) {
                paramsMap.put(values[0], values[1]);
            } else if (values.length == 1 ) {
                paramsMap.put(values[0], null);
            } else {
                System.err.println("Error while parsing URI query");
            }
        }
        return paramsMap;
    }

    /**
     * Get and return a file at a given path
     * @param path
     * @return File if the file has been found, or null otherwise
     */
    private File getFile(String path) {
        File f = new File(WEB_ROOT_DIRECTORY, path);

        if(f.exists() && !f.isDirectory()) {
            return f;
        }
        return null;
    }

    private boolean getKeepAlive(HttpExchange httpExchange) {
        boolean keepAlive = true;
        Headers headers = httpExchange.getRequestHeaders();
        if (headers.containsKey("Connection")) {
            for (String value : headers.get("Connection")) {
                if (value.equalsIgnoreCase("Close")) {
                    keepAlive = false;
                }
            }
        }
        System.out.println("Persistent connection: " + keepAlive);
        return keepAlive;
    }
}
