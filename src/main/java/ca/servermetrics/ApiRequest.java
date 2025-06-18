package main.java.ca.servermetrics;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiRequest {
    private String method;
    private String endpoint;
    private String body;
    public ApiRequest(String method, String endpoint, String body){
        this.method = method;
        this.endpoint = endpoint;
        this.body = body;
    }

    public POST(){
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://192.168.0.189:8000/" + this.endpoint))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(this.body))
        .build();

    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .thenAccept(response -> {
        })
        .exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }
}
