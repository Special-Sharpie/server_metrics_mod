package main.java.ca.servermetrics;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import main.java.ca.servermetrics.ApiRequest;
import main.java.ca.servermetrics.ServiceStatus;


public class ResourceUsage implements Runnable{

    private final ServiceStatus status;

    public static final String MOD_ID = "servermetrics";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public ResourceUsage(ServiceStatus status){
        this.status = status;
    }

    @Override
    public void run(){
        ApiRequest req = new ApiRequest(status, "resources");
        while (true){
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory() /1000000 ;
            long freeMemory = runtime.freeMemory() /1000000 ;
            long maxMemory = runtime.maxMemory() /1000000 ;
            int availableProcessors = runtime.availableProcessors();

            OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            double cpuLoad = osBean.getCpuLoad() * 100;
            
            req.POST(buildBody(totalMemory, maxMemory, cpuLoad));
            // LOGGER.info("Memory Usage: " + totalMemory + "MB / " + maxMemory + "MB \n " + freeMemory  + " MB \n CPU Usage " + cpuLoad);
            


            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                LOGGER.error("Resource Usage Thread Interrupted");
            }
            
        }
    }
    public static String buildBody(long totalMemory, long maxMemory, double cpuLoad){
		return "{\"totalMemory\":\""+totalMemory+"\",\"maxMemory\":"+maxMemory+",\"cpuUsage\":"+cpuLoad+"}";
	}


}
