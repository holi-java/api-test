package test.java.net;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static java.net.NetworkInterface.getNetworkInterfaces;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by holi on 4/17/17.
 */
public class HttpsServerTest {
    private static final String KEYSTORE_RESOURCE_PATH = "keystore.jks";
    private static final String CERT_RESOURCE_PATH = "server.crt";
    private final int serverPort = 9900;
    private final char[] password = "password".toCharArray();
    private final Server server = new Server(classpath(KEYSTORE_RESOURCE_PATH), password, serverPort);

    @BeforeEach
    void startServer() throws Throwable {
        server.start();
    }

    @Test
    void requestServerUsingCommonName() throws Throwable {
        assertSSLServerCommunication("localhost");
    }

    @Test
    void requestServerUsingAlternativeName() throws Throwable {
        assertSSLServerCommunication("127.0.0.1");
    }

    @Test
    void failsToHandshakeWhenRequestServerUsingAnUnknownName() throws Throwable {
        String address = localAddress();
        assertThat(address, startsWith("192.168."));
        String expectedMessage = String.format("No subject alternative names matching IP address %s found", address);

        Throwable error = assertThrows(SSLHandshakeException.class, () -> request(address, "<anything>"));

        assertThat(error.getCause().getMessage(), equalTo(expectedMessage));
    }

    @AfterEach
    void stopServer() throws Throwable {
        server.stop();
    }

    private void assertSSLServerCommunication(String hostName) throws Exception {
        HttpsURLConnection connection = request(hostName, "foo");
        server.hasReceivedRequestBody("foo");

        server.render("bar");
        assertThat(response(connection), equalTo("bar"));
    }

    private String localAddress() throws SocketException {
        return Collections.list(getNetworkInterfaces()).stream()
                .flatMap(it -> Collections.list(it.getInetAddresses()).stream())
                .filter(test(InetAddress::isSiteLocalAddress).
                        and(test(InetAddress::isLoopbackAddress).negate()).
                        and(Inet4Address.class::isInstance))
                .findFirst()
                .map(InetAddress::getHostAddress).orElseThrow(NoSuchElementException::new);
    }

    private static <T> Predicate<T> test(Predicate<T> condition) {
        return condition;
    }

    private HttpsURLConnection request(String hostName, String body) throws Exception {
        byte[] content = body.getBytes();
        HttpsURLConnection it = connect(String.format("https://%s:%d", hostName, serverPort));

        it.setDoOutput(true);
        it.setFixedLengthStreamingMode(content.length);
        try (OutputStream out = it.getOutputStream()) {
            out.write(content);
        }
        return it;
    }

    private String response(HttpsURLConnection connection) throws IOException {
        try (InputStream it = connection.getInputStream()) {
            return stringify(it);
        }
    }

    private HttpsURLConnection connect(String url) throws Exception {
        HttpsURLConnection it = (HttpsURLConnection) new URL(url).openConnection();
        it.setSSLSocketFactory(createSSLSocketFactory(cert(classpath(CERT_RESOURCE_PATH))));
        return it;
    }

    private class Server {
        private static final int DEFAULT_BACKLOG = 0;
        private static final int IMMEDIATELY = 0;

        private HttpsServer server;
        private int serverPort;
        private InputStream keystore;
        private char[] password;

        private BlockingQueue<String> incoming = new ArrayBlockingQueue<>(1);
        private BlockingQueue<String> outgoing = new ArrayBlockingQueue<>(1);

        public Server(InputStream keystore, char[] password, int serverPort) {
            this.serverPort = serverPort;
            this.keystore = keystore;
            this.password = password;
        }

        public void start() throws Exception {
            server = HttpsServer.create(new InetSocketAddress(serverPort), DEFAULT_BACKLOG);
            server.setHttpsConfigurator(configuration());
            server.createContext("/", this::serve);
            server.start();
        }

        private HttpsConfigurator configuration() throws Exception {
            return new HttpsConfigurator(createSSLContext(keystore, password));
        }

        private void serve(HttpExchange it) throws IOException {
            incoming.add(stringify(it.getRequestBody()));
            respond(it, body());
            it.close();
        }

        private void respond(HttpExchange it, byte[] body) throws IOException {
            it.sendResponseHeaders(200, body.length);
            try (OutputStream out = it.getResponseBody()) {
                out.write(body);
            }
        }

        private byte[] body() throws IOException {
            try {
                return outgoing.take().getBytes();
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }

        public void hasReceivedRequestBody(String body) throws InterruptedException {
            assertThat("incoming", incoming.poll(1, TimeUnit.SECONDS), equalTo(body));
        }

        public void render(String body) {
            outgoing.add(body);
        }


        public void stop() {
            if (server != null) {
                server.stop(IMMEDIATELY);
            }
        }

    }

    private static String stringify(InputStream in) throws IOException {
        ByteArrayOutputStream it = new ByteArrayOutputStream();
        copy(in, it);
        return it.toString();
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buff = new byte[1024];
        for (int n; (n = in.read(buff)) != -1; ) {
            out.write(buff, 0, n);
        }
    }

    private static SSLSocketFactory createSSLSocketFactory(Certificate cert) throws Exception {
        return createSSLContext(cert).getSocketFactory();
    }

    private static SSLContext createSSLContext(Certificate ca) throws Exception {
        return createSSLContext(keystore(ca), null);
    }

    private static SSLContext createSSLContext(InputStream keystore, char[] password) throws Exception {
        return createSSLContext(keystore(keystore, password), password);
    }

    private static SSLContext createSSLContext(KeyStore keystore, char[] password) throws Exception {
        SSLContext it = SSLContext.getInstance("TLS");
        it.init(keyManagers(keystore, password), trustManagers(keystore), null);
        return it;
    }

    private static KeyStore keystore(Certificate ca) throws Exception {
        KeyStore it = keystore(null, null);
        it.setCertificateEntry("ca", ca);
        return it;
    }

    private static KeyStore keystore(InputStream keystore, char[] password) throws Exception {
        KeyStore it = KeyStore.getInstance("JKS");
        it.load(keystore, password);
        return it;
    }

    private static TrustManager[] trustManagers(KeyStore keyStore) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory it = TrustManagerFactory.getInstance("SunX509");
        it.init(keyStore);
        return it.getTrustManagers();
    }

    private static KeyManager[] keyManagers(KeyStore keystore, char[] password) throws Exception {
        KeyManagerFactory it = KeyManagerFactory.getInstance("SunX509");
        it.init(keystore, password);
        return it.getKeyManagers();
    }

    private static InputStream classpath(String resource) {
        return Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(resource),
                () -> "Resource not found in classpath: " + resource);
    }

    private static Certificate cert(InputStream cert) throws CertificateException, IOException {
        return CertificateFactory.getInstance("X.509").generateCertificate(cert);
    }
}
