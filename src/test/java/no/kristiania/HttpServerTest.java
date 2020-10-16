package no.kristiania;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpServerTest {

    @Test
    void shouldReturnSuccessfulErrorCode() throws IOException {
        HttpServer server = new HttpServer(10001);
        HttpClient client = new HttpClient("localhost", 10001, "/echo");
        assertEquals(200, client.getStatusCode());
    }

    @Test
    void shouldReturnUnsuccessfulErrorCode() throws IOException {
        HttpServer server = new HttpServer(10002);
        HttpClient client = new HttpClient("localhost", 10002, "/echo?status=404");
        assertEquals(404, client.getStatusCode());
    }
    @Test
    void shouldReturnHttpHeaders() throws IOException {
        new HttpServer(10003);
        HttpClient client = new HttpClient("localhost", 10003, "/echo?body=HelloWorld");
        assertEquals("10", client.getResponseHeader("Content-Length"));
    }
    @Test
    void shouldReturnFileContent()throws IOException{
        HttpServer server = new HttpServer(10005);
        File documentRoot = new File("target/");
        server.setDocumentRoot(documentRoot);

        String fileContent = "Hello" + new Date();
        Files.writeString(new File(documentRoot,"index.html").toPath(),fileContent);
        HttpClient client = new HttpClient("localhost",10005,"/index.html");
        assertEquals(fileContent, client.getResponseBody());
    }
    @Test
    void shouldReturn404onMissingFile() throws IOException{
        HttpServer server = new HttpServer(10006);
        server.setDocumentRoot(new File("target/"));
        HttpClient client = new HttpClient("localhost", 10006,"/missingfile");
        assertEquals(404, client.getStatusCode());
    }
    @Test
    void shouldReturnCorrectContentType() throws IOException{
        HttpServer server = new HttpServer(10007);
        File documentRoot = new File ("target/");
        server.setDocumentRoot(documentRoot);
        Files.writeString(new File(documentRoot, "plain.txt").toPath(),"Plain.txt");
        HttpClient client = new HttpClient("localhost", 10007,"/plain.txt");
        assertEquals("text/plain", client.getResponseHeader("Content-Type"));
    }
    @Test
    void shouldPostMember() throws IOException{
        HttpServer server = new HttpServer(10008);
        QueryString member = new QueryString("");
        member.addParameter("memberName", "Erik");
        member.addParameter("memberStatus", "Student");
        HttpClient client = new HttpClient("localhost", 10008,"/api/addMember", "POST", member);
        assertEquals(200, client.getStatusCode());
        assertEquals(List.of("Erik"), server.getMemberNames());
    }

    @Test
    void shouldDisplayExistingProducts() throws IOException{
        HttpServer httpServer = new HttpServer(10009);
        httpServer.getMemberNames().add("Erik");
        HttpClient client = new HttpClient("localhost", 10009, "/api/members");
        assertEquals("<ul><li>Erik</li></ul>", client.getResponseBody());
    }
}
