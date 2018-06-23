import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler implements HttpHandler {


    private static int STATUS_OK = 200;
    private static int STATUS_CREATED = 201;
    private static int STATUS_BAD_REQUEST = 400;
    private static int STATUS_FORBIDDEN = 403;
    private static int STATUS_NOT_FOUND = 404;
    private static int STATUS_SERVER_ERROR = 500;

    private static String WEB_ROOT_DIRECTORY = "/web";

    @Override
    // Only use local variable to be thread safe
    public void handle(HttpExchange httpExchange) throws IOException {
        String clientAddr = httpExchange.getRemoteAddress().getHostString();
        System.out.println("\nHandle request for " + clientAddr);

        // Only support HTTP/1.1 protocol
        if (!httpExchange.getProtocol().equalsIgnoreCase("HTTP/1.1")){
            System.err.println("Unsupported http protocol: " + httpExchange.getProtocol());
            httpExchange.sendResponseHeaders(STATUS_BAD_REQUEST, 0);
            httpExchange.close();
            return;
        }

        // Get Keep-alive header
        boolean keepAlive = getKeepAlive(httpExchange);

        // Get Method
        String method = httpExchange.getRequestMethod();
        System.out.println("Method: " + method);
        switch (method) {
            case "GET":
                handleGetMethod(httpExchange, clientAddr);
                break;
            case "POST":
                handlePostMethod(httpExchange, clientAddr);
                break;
            default:
                System.err.println("\tUnsupported Http method: " + method);
                httpExchange.sendResponseHeaders(STATUS_BAD_REQUEST, 0);
                httpExchange.close();
                break;
        }

        // Close connection if there is no Keep-Alive header
        if (!keepAlive) {
            System.out.println("Closing connection of " + clientAddr);
            httpExchange.close();
        }
    }

    /**
     * Handle GET method. Returns the content of the requested file if found. Otherwise send 404 response
     * @param httpExchange HttpExchange object
     * @param clientAddr ip address of the client
     * @throws IOException
     */
    private synchronized void handleGetMethod(HttpExchange httpExchange, String clientAddr) throws IOException {
        // Get requested path
        String path = httpExchange.getRequestURI().getPath();

        System.out.println(clientAddr + " requested " + getAbsolutePath(path));

        // Get file for the response
        File file = getFile(path);
        if (file == null) {
            System.err.println(getAbsolutePath(path) + ": file not found");
            httpExchange.sendResponseHeaders(STATUS_NOT_FOUND, 0);
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
            OutputStream os = httpExchange.getResponseBody();
            os.write(b,0, b.length);
            os.flush();
            os.close();
            System.out.println("Content sent to " + clientAddr + " with " + STATUS_OK + " response");
        }

    }

    /**
     * Handle POST method. Post the content of the query in json format in the file.
     * Send forbidden response
     * @param httpExchange HttpExchange object
     * @param clientAddr ip address of the client
     * @throws IOException
     */
    private synchronized void handlePostMethod(HttpExchange httpExchange, String clientAddr) throws IOException {
        // Get requested path
        String path = httpExchange.getRequestURI().getPath();

        System.out.println(clientAddr + " requested " + getAbsolutePath(path));

        // only access to post.html.html file - do not modify index.html and directory/file.html
        // return 403 forbidden
        File file = getFile(path);
        if (file == null) {
            System.err.println(getAbsolutePath(path) + ": file not found");
            httpExchange.sendResponseHeaders(STATUS_NOT_FOUND, 0);
        }
        else if (!path.equalsIgnoreCase("/post.html")) {
            System.err.println(getAbsolutePath(path) + ": resources forbidden with post method");
            httpExchange.sendResponseHeaders(STATUS_FORBIDDEN, 0);
        }
        else {
            // Get Query parameters
            String paramString = httpExchange.getRequestURI().getRawQuery();
            Map<String, String> paramsMap = null;
            if (paramString != null) {
                paramsMap = getParams(paramString);
            }
            if (paramsMap == null ) {
                httpExchange.sendResponseHeaders(STATUS_BAD_REQUEST, 0);
            }
            else {
                // Just write content into file
                JSONObject jsonObject = new JSONObject(paramsMap);
                FileWriter fw = new FileWriter(getAbsolutePath(path));
                fw.write(jsonObject.toString() + "\n");
                fw.flush();
                System.out.println(jsonObject.toString() + " written to " + getAbsolutePath(path));
                httpExchange.sendResponseHeaders(STATUS_CREATED, 0);
                System.out.println("Response with " + STATUS_CREATED);
            }
        }
    }

    /**
     * Returns an absolute path for a given relative path
     * @param path relative path
     * @return absolute path
     */
    // TODO: make a global variable for the root path. Not having to recalculate each time
    private String getAbsolutePath(String path) {
        String[] pathSplit = System.getProperty("user.dir").split(File.separator);

        // Get the last occurence of "MultithreadedPoolingServer" in the path
        int index = pathSplit.length-1;
        for (int i = pathSplit.length-1; i > 0; i--){
            if (pathSplit[i].equalsIgnoreCase("MultithreadedPoolingServer")){
                index = i;
                break;
            }
        }

        // Compute the absolute path
        StringBuilder absolutePath = new StringBuilder();
        for (int i = 0; i <= index; i++) {
            absolutePath.append(File.separator).append(pathSplit[i]);
        }
        absolutePath.append(WEB_ROOT_DIRECTORY).append(path);

        return absolutePath.toString();
    }

    /**
     * Return a Map of every parameters in the request paramsString
     * @param paramsString query string
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
                return null;
            }
        }
        return paramsMap;
    }

    /**
     * Get and return a file at a given path
     * @param path path of the file
     * @return File if the file has been found, or null otherwise
     */
    private File getFile(String path) {
        File f = new File(getAbsolutePath(path));

        if(f.exists() && !f.isDirectory()) {
            return f;
        }
        return null;
    }

    /**
     * Test if the Http header contains the 'Connection' header, and then if this header is set to 'Close'
     * @param httpExchange HttpExchange object
     * @return boolean. False if 'Connection: Close' is set, True otherwise
     */
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
