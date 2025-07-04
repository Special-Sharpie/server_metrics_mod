package main.java.ca.servermetrics;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.ca.servermetrics.ServiceStatus;


public class ServiceWatcher implements Runnable {
    private final ServiceStatus status;
    public volatile boolean offline = true;
    public static final String MOD_ID = "servermetrics";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    

    public ServiceWatcher(ServiceStatus status){
        this.status = status;
    }

    @Override
    public void run(){
        while (offline){
            // Leaving short for testing, might make longer in the future
            LOGGER.warn("Server Metrics service unreachable | Sleeping service for 1 minute!");
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                LOGGER.error("Service Watcher Thread Interrupted");
            }
            
            LOGGER.warn("Attempting to connect to Server metrics service");
            if (getServerStatus()){
                status.offline = false;
                break;
            }
        }
    }

    public static boolean getServerStatus(){
		HttpClient client = HttpClient.newHttpClient();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://192.168.1.70:8000/online"))
				.GET()
				.timeout(Duration.ofSeconds(10))
				.build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			return "true".equals(response.body());
        } catch (IOException | InterruptedException e) {
            // e.printStackTrace();
        }
		return false;
	}
}
