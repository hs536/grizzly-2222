package test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.ContentType;

public class Server {
    private static final Logger LOGGER = Grizzly.logger(Server.class);

    public static void main(String[] args) {
        final HttpServer server = new HttpServer();
        final ServerConfiguration config = server.getServerConfiguration();
        config.addHttpHandler(
            new HttpHandler() {
                @Override
                public void service(Request request, Response response) throws Exception {
                    String data = "Hello Grizzly!!";
                    response.setContentType(ContentType.newContentType("text/plain", "UTF-8"));
                    response.setContentLength(data.getBytes(StandardCharsets.UTF_8).length);
                    response.getWriter().write(data);
                }
            }, "/grizzly");
        final NetworkListener networkListener = new NetworkListener("listener", 
            NetworkListener.DEFAULT_NETWORK_HOST,
            NetworkListener.DEFAULT_NETWORK_PORT);
        server.addListener(networkListener);
        try {
            server.start();
            System.out.println("Press any key to stop the server...");
            System.in.read();
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, ioe.toString(), ioe);
        } finally {
            server.shutdownNow();
        }
    }
}
