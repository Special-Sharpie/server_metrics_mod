package ca.servermetrics;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.ca.servermetrics.PlayerPositionLogger;
import main.java.ca.servermetrics.LogInterceptor;
import main.java.ca.servermetrics.ApiRequest;
import main.java.ca.servermetrics.ServiceStatus;
import main.java.ca.servermetrics.ServiceWatcher;
import main.java.ca.servermetrics.ResourceUsage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class FabricMain implements ModInitializer {
	public static final String MOD_ID = "servermetrics";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ServiceStatus status = new ServiceStatus();
		new Thread(new ResourceUsage(status)).start();
		ApiRequest req = new ApiRequest(status, "event");
		LOGGER.info("Server Metrics initializing...");
		getServerStatus(status);
		LogInterceptor.init(status);
		// Player joins
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			req.POST(buildBody(player.getName().getString(), true, (int)(System.currentTimeMillis() / 1000L), player.getUuid().toString()));
		});

		// PlayerPositionLogger.register(status);
		// Player leaves
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			req.POST(buildBody(player.getName().getString(), false, (int)(System.currentTimeMillis() / 1000L), null));

		});
	}

	public static String buildBody(String name, Boolean event, Integer timestamp, String uuid){
		if (uuid != null){
			return "{\"username\":\""+name+"\",\"event\":"+event+",\"timestamp\":"+timestamp+ ",\"uuid\":\""+uuid+"\"}";
		}else{
			return "{\"username\":\""+name+"\",\"event\":"+event+",\"timestamp\":"+timestamp+"}";
		}
	}
	public static void getServerStatus(ServiceStatus status){
		HttpClient client = HttpClient.newHttpClient();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://192.168.1.70:8000/online"))
				.GET()
				.timeout(Duration.ofSeconds(10))
				.build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
			if (!status.offline) {
				status.offline = true;
				new Thread(new ServiceWatcher(status)).start();
			}
        }
	}
}