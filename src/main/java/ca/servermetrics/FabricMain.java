package ca.servermetrics;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.ca.servermetrics.PlayerPositionLogger;
import main.java.ca.servermetrics.LogInterceptor;

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
		LOGGER.info("Server Metrics initializing...");
			if (getServerStatus()){
			LogInterceptor.init();
			// Player joins
			ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
				ServerPlayerEntity player = handler.getPlayer();
				LOGGER.warn("This is a test warning");
				LOGGER.error("This is a test error");
				postConnectionEvent(player.getName().getString(), true, (int)(System.currentTimeMillis() / 1000L));
			});

			PlayerPositionLogger.register();
			// Player leaves
			ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
				ServerPlayerEntity player = handler.getPlayer();
				System.out.println("[LEAVE] " + player.getName().getString());
				postConnectionEvent(player.getName().getString(), false, (int)(System.currentTimeMillis() / 1000L));

			});
		}else{
			LOGGER.info("Server Metric Services unreachable... Skipping initialization...");
		}

	}

	public static void postConnectionEvent(String name, Boolean event, Integer timestamp){
		HttpClient client = HttpClient.newHttpClient();

		String json = "{\"username\":\""+name+"\",\"event\":"+event+",\"timestamp\":"+timestamp+"}";

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://192.168.0.189:8000/event"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(json))
				.build();

		client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
				.thenAccept(response -> {
				})
				.exceptionally(ex -> {
					ex.printStackTrace();
					return null;
				});
	}
	public static boolean getServerStatus(){
		HttpClient client = HttpClient.newHttpClient();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://192.168.0.189:8000/online"))
				.GET()
				.timeout(Duration.ofSeconds(10))
				.build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			return "true".equals(response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
		return false;
	}
}