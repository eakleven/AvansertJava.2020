package no.kristiania;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class HttpServer {


    private static File documentRoot;
    private List<String> memberNames = new ArrayList<>();

    public HttpServer(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);

        new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();

                    handleRequest(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    private void handleRequest(Socket socket) throws IOException {
        String requestLine = HttpMessage.readLine(socket);
        System.out.println(requestLine);

        String requestMethod = requestLine.split(" ")[0];
        String requestTarget = requestLine.split(" ")[1];

        if (requestMethod.equals("POST")) {
            HttpMessage requestMessage = new HttpMessage(requestLine);
            requestMessage.readHeaders(socket);
            QueryString requestForm = new QueryString(requestMessage.readBody(socket));
            memberNames.add(requestForm.getParameter("memberName"));


            writeResponse(socket, "200", "ok");

            return;
        }

        String statusCode = null;
        String body = null;


        int questionPos = requestTarget.indexOf('?');
        if (questionPos != -1) {
            QueryString queryString = new QueryString(requestTarget.substring(questionPos + 1));
            statusCode = queryString.getParameter("status");
            body = queryString.getParameter("body");
        } else if (!requestTarget.equals("/echo")) {
            File targetFile = new File(documentRoot, requestTarget);

            if (!targetFile.exists()) {
                writeResponse(socket, "404", requestTarget + " not found");
                return;
            }
            HttpMessage responseMessage = new HttpMessage("HTTP/1.1 200 OK");
            responseMessage.setHeaders("Content-Length", String.valueOf(targetFile.length()));
            responseMessage.setHeaders("Connection", "close");
            responseMessage.setHeaders("Content-Type", "text/html");

            String contentType = "text/html";
            if (targetFile.getName().endsWith(".txt")) {
                contentType = "text/plain";
                responseMessage.setHeaders("Content-Type", "text/plain");
            }
            else if (targetFile.getName().endsWith(".css")) {
                contentType = "text/css";
                responseMessage.setHeaders("Content-Type", "text/css");
            }
            responseMessage.write(socket);

            try (FileInputStream inputStream = new FileInputStream(targetFile)) {
                inputStream.transferTo(socket.getOutputStream());
            }
            return;
        }
        if (statusCode == null) statusCode = "200";
        if (body == null) body = "Hello <strong>world</strong>!";

        writeResponse(socket, statusCode, body);

    }

    private void writeResponse(Socket clientSocket, String statusCode, String body) throws IOException {
        String response = "HTTP/1.1 " + statusCode + " OK\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                body;

        //clientSocket.getOutputStream().write(response.getBytes());

        HttpMessage responseMessage = new HttpMessage("HTTP/1.1 " + statusCode + " OK");
        responseMessage.setHeaders("Content-Length", String.valueOf(body.length()));
        responseMessage.setHeaders("Content-Type", "text/plain");
        responseMessage.write(clientSocket);
        clientSocket.getOutputStream().write(body.getBytes());
    }


    public static void main(String[] args) throws IOException {
        HttpServer server = new HttpServer(8080);
        server.setDocumentRoot(new File("src/main/resources/"));

    }


    public void setDocumentRoot(File documentRoot) {
        this.documentRoot = documentRoot;
    }

    public List<String> getMemberNames() {
        return memberNames;
    }
}




