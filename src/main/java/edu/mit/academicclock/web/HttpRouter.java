package edu.mit.academicclock.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.concurrent.Executors;

/**
 * Unit V: Web Development & Java HTTP Server
 * Serves REST API routes and static frontend files (HTML/CSS/JS).
 */
public class HttpRouter {
    private final int port;
    private final ApiHandlers apiHandlers;
    private final File publicDir;
    private HttpServer server;

    public HttpRouter(int port, ApiHandlers apiHandlers, File publicDir) {
        this.port = port;
        this.apiHandlers = apiHandlers;
        this.publicDir = publicDir;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // API Handler
        server.createContext("/api", apiHandlers::handle);

        // Static Files Handler
        server.createContext("/", new StaticFileHandler(publicDir));

        // Use thread pool for handling incoming requests (Unit III: Multithreading)
        server.setExecutor(Executors.newFixedThreadPool(16));
        server.start();

        System.out.println("✨ ClockIt Server running at: http://localhost:" + port);
        System.out.println("   - REST API Root: http://localhost:" + port + "/api/overview");
        System.out.println("   - Interactive Frontend: http://localhost:" + port + "/");
    }

    public void stop() {
        if (server != null) {
            server.stop(1);
            System.out.println("ClockIt Server stopped.");
        }
    }

    private static class StaticFileHandler implements HttpHandler {
        private final File baseDir;

        public StaticFileHandler(File baseDir) {
            this.baseDir = baseDir;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            File requestedFile = new File(baseDir, path.substring(1));
            // Prevent directory traversal attacks
            if (!requestedFile.getCanonicalPath().startsWith(baseDir.getCanonicalPath())) {
                send404(exchange);
                return;
            }

            if (!requestedFile.exists() || requestedFile.isDirectory()) {
                // SPA Fallback: serve index.html for unknown client routes
                requestedFile = new File(baseDir, "index.html");
            }

            if (!requestedFile.exists()) {
                send404(exchange);
                return;
            }

            String contentType = determineContentType(requestedFile.getName());
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Cache-Control", "no-cache");
            exchange.sendResponseHeaders(200, requestedFile.length());

            try (FileInputStream fis = new FileInputStream(requestedFile);
                 OutputStream os = exchange.getResponseBody()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        }

        private String determineContentType(String name) {
            if (name.endsWith(".html")) return "text/html; charset=UTF-8";
            if (name.endsWith(".css")) return "text/css; charset=UTF-8";
            if (name.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (name.endsWith(".json")) return "application/json; charset=UTF-8";
            if (name.endsWith(".png")) return "image/png";
            if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
            if (name.endsWith(".svg")) return "image/svg+xml";
            if (name.endsWith(".ico")) return "image/x-icon";
            return "application/octet-stream";
        }

        private void send404(HttpExchange exchange) throws IOException {
            byte[] msg = "404 Not Found".getBytes();
            exchange.sendResponseHeaders(404, msg.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(msg);
            }
        }
    }
}
