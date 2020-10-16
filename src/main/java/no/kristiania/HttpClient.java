package no.kristiania;

import java.io.IOException;
import java.net.Socket;

public class HttpClient {

    private String responseBody;
    private final HttpMessage httpMessage;

    public HttpClient(final String hostname, int port, final String requestTarget) throws IOException {

        Socket socket = new Socket(hostname, port);

        HttpMessage requestMessage = new HttpMessage("GET " + requestTarget + " HTTP/1.1");
        requestMessage.setHeaders("Host", hostname);
        requestMessage.write(socket);

        httpMessage = HttpMessage.read(socket);
        responseBody = httpMessage.readBody(socket);

    }

    public HttpClient(String hostname, int port, String requestTarget, String method, QueryString form) throws IOException {
        Socket socket = new Socket(hostname, port);

        String requestBody = form.getQueryString();

        HttpMessage requestMessage = new HttpMessage(method + " " + requestTarget + " HTTP/1.1");
        requestMessage.setHeaders("Host", hostname);
        requestMessage.setHeaders("Content-Length", String.valueOf(requestBody.length()));
        requestMessage.write(socket);
        socket.getOutputStream().write(requestBody.getBytes());

        httpMessage = HttpMessage.read(socket);
    }


    public static void main(String[] args) throws IOException {
        new HttpClient("urlecho.appspot.com", 80, "/echo?status=200&body=Hello%20world!");
    }

    public int getStatusCode() {
        String[] responseLineParts = httpMessage.getStartLine().split(" ");
        return Integer.parseInt(responseLineParts[1]);
    }

    public String getResponseHeader(String headerName) {
        return httpMessage.getHeader(headerName);

    }

    public String getResponseBody() {
        return responseBody;
    }
}
