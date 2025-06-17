package main.java.ca.servermetrics;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import net.minecraft.util.math.Vec3d;

public class PlayerPositionLogger {
    private static final Map<UUID, Vec3d> lastPositions = new HashMap<>();
    public static final String MOD_ID = "servermetrics";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
   
    private static int tickCount = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCount++;
            if (tickCount % 20 == 0) { // ~every 1 second (20 ticks)
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    UUID pid = player.getUuid();
                    Vec3d pos = player.getPos();  // Safe: running on main server thread
                    Vec3d lastPos = lastPositions.get(pid);
                    if (lastPos == null || !pos.equals(lastPos)) {
                        lastPositions.put(pid, pos);
                        postPosition(
                            (int)pos.x, 
                            (int)pos.y, 
                            (int)pos.z, 
                            player.getName().getString(), 
                            (int)(System.currentTimeMillis() / 1000L)
                        );
                    }
                }
            }
        });
    }
    public static void postPosition(int x, int y, int z, String name, int timestamp){
		HttpClient client = HttpClient.newHttpClient();

		String json = "{\"username\":\"" + name + "\","
            + "\"timestamp\":" + timestamp + ","
            + "\"x\":" + x + ","
            + "\"y\":" + y + ","
            + "\"z\":" + z + "}";

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://192.168.0.189:8000/position"))
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
}