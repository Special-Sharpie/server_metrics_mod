package main.java.ca.servermetrics;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import main.java.ca.servermetrics.ServiceStatus;

public class ApiRequest {
    private ServiceStatus status;
    private String endpoint;

    public ApiRequest(ServiceStatus status, String endpoint){
        this.status = status;
        this.endpoint = endpoint;
    }

    public void POST(String body){
        if (!status.offline){
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://192.168.0.189:8000/" + this.endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                })
                .exceptionally(ex -> {
                    if (!status.offline) {
                        status.offline = true;
                        new Thread(new ServiceWatcher(status)).start();
                    }
                    // ex.printStackTrace();
                    return null;
            });
        }
    }
}
