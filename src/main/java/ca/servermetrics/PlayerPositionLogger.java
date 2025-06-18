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

import main.java.ca.servermetrics.ApiRequest;
import main.java.ca.servermetrics.ServiceStatus;

public class PlayerPositionLogger {
    private static final Map<UUID, Vec3d> lastPositions = new HashMap<>();
    public static final String MOD_ID = "servermetrics";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
   
    private static int tickCount = 0;

    public static void register() {
        ServiceStatus status = new ServiceStatus();
		ApiRequest req = new ApiRequest(status, "position");
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCount++;
            if (tickCount % 20 == 0) { // ~every 1 second (20 ticks)
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    UUID pid = player.getUuid();
                    Vec3d pos = player.getPos();  // Safe: running on main server thread
                    Vec3d lastPos = lastPositions.get(pid);
                    if (lastPos == null || !pos.equals(lastPos)) {
                        lastPositions.put(pid, pos);
                        req.POST(buildBody(
                            (int)pos.x, 
                            (int)pos.y, 
                            (int)pos.z, 
                            player.getName().getString(), 
                            (int)(System.currentTimeMillis() / 1000L)
                        ));
                    }
                }
            }
        });
    }
    public static String buildBody(int x, int y, int z, String name, int timestamp){
		return "{\"username\":\"" + name + "\","
            + "\"timestamp\":" + timestamp + ","
            + "\"x\":" + x + ","
            + "\"y\":" + y + ","
            + "\"z\":" + z + "}";
	}
}