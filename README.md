# [MultithreadedPoolingServer](https://github.com/iAmoric/MultithreadedPoolingServer)

This project is a multi-threaded web server with thread-pooling implemented in Java. It was made for the Adobe hiring procedure.

## Getting started

### Using the jar file

You can run the `.jar` file located in the `out/` folder with 

`java -jar out/MultithreadedPoolingServer.jar [port]`

### How to use

The web server is listening on the port `8080` by default. You can specify your own port with the first argument. For example, to listen on 4200:

`java -jar out/MultithreadedPoolingServer.jar 4200`

The server is multi-thread, and use a thread pool. The thread pool is automatically set to the number of available processors on the machine.

The server only supports the `HTTP/1.1` protocol, and is able to handle `GET` and `POST` method.

As this project is just an example of a web server, it only serves the pages `/index.html`, `/post.html` and `/directory/file.html`. These files are located in the `web/` directory. For example, you can make a `GET` request to `index.html` by going to:

`localhost:8080/index.html`

The page `/post.html` can be accessed via `GET` and `POST` requests, but the pages `/index.html` and `/directory/file.html` are only accessible with `GET` requests.
If you try to make a `POST` request on these pages, the server will return a `403 forbidden` error.

For the `POST` requests, you can pass parameters via the url, like for example:

`localhost:8080/post.html?param1=value1&param2=value2`

For this project, I've chosen to convert the parameters into JSON object, and then just print it on the `post.html` file. It is only to illustrate a `POST` request.

This server supports the keep-alive connection with the specific request headers from the client. If the request header contains `Connection: Close` (as specifiy for `HTTP/1.1`), the connection is closed after the server has responded to the client. Otherwise, the connection is kept open, since that's the default behavior of `HTTP/1.1`.

## Author

* [**Lucas Pierrat**](https://github.com/iAmoric) - [contact](mailto:pierratlucas@gmail.com)

## License

This project is licensed under the MIT License - see the [LICENSE](https://github.com/iAmoric/MultithreadedPoolingServer/blob/master/LICENSE) file for details

    