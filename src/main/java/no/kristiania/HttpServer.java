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
        HttpMessage request = new HttpMessage(socket);
        String requestLine = request.getStartLine();
        System.out.println(requestLine);

        System.out.println("REQUEST " + requestLine);

        String requestMethod = requestLine.split(" ")[0];
        String requestTarget = requestLine.split(" ")[1];

        int questionPos = requestTarget.indexOf('?');

        String requestPath = questionPos != -1 ? requestTarget.substring(0, questionPos) : requestTarget;

        if (requestMethod.equals("POST")) {
            QueryString requestForm = new QueryString(request.getBody());

            memberNames.add(requestForm.getParameter("memberName"));
            String body = "Okay";


            writeResponse(socket, "200", body);

            return;
        } else {
            if (requestPath.equals("/echo")) {
                handleEchoRequest(socket, requestTarget, questionPos);
            } else if (requestPath.equals("/api/members")) {
                handleGetMembers(socket);
            } else{
                handleFileRequest(socket, requestPath);
            }
        }
    }

    private void handleGetMembers(Socket socket) throws IOException {
        String body = "<ul>";
        for (String memberName : memberNames) {
            body += "<li>" + memberName + "</li>";
        }
        body += "</ul>";
        writeResponse(socket, "200", body);
    }

    private void handleEchoRequest(Socket socket, String requestTarget, int questionPos) throws IOException{
        String statusCode = "200";
        String body = "Hello World";
        if (questionPos != -1){
            QueryString queryString = new QueryString(requestTarget.substring(questionPos+1));
            if (queryString.getParameter("status") !=null){
                statusCode = queryString.getParameter("status");
            }
            if (queryString.getParameter("body") != null){
                body = queryString.getParameter("body");
            }
        }
        writeResponse(socket, statusCode, body);

    }

    private void handleFileRequest(Socket socket, String requestTarget) throws IOException {
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

    private void writeResponse(Socket clientSocket, String statusCode, String body) throws IOException {


        HttpMessage responseMessage = new HttpMessage("HTTP/1.1 " + statusCode + " OK");
        responseMessage.setHeaders("Content-Length", String.valueOf(body.length()));
        responseMessage.setHeaders("Connection", "Close");
        responseMessage.setHeaders("Content-Type", "text/plain");
        responseMessage.write(clientSocket);
        clientSocket.getOutputStream().write(body.getBytes());
    }


    public static void main(String[] args) throws IOException {
        System.out.println("Running");
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




