package no.kristiania.httpclient;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class queryStringTest {
    @Test
    void shouldRetrieveQueryParameter(){
        QueryString queryString = new QueryString("status-200");
        assertEquals("200", queryString.getParameter("status"));
    }

}
