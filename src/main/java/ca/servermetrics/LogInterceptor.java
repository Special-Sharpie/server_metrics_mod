package main.java.ca.servermetrics;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.LoggerContext;

import main.java.ca.servermetrics.ApiRequest;
import main.java.ca.servermetrics.ServiceStatus;

public class LogInterceptor {

    public static void init() {
        ServiceStatus status = new ServiceStatus();
		ApiRequest req = new ApiRequest(status, "logs");
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        LoggerConfig loggerConfig = ctx.getConfiguration().getRootLogger();
        Appender customAppender = new AbstractAppender("CustomAPIAppender", null, null, false, null) {
            @Override
            public void append(LogEvent event) {
                String message = event.getMessage().getFormattedMessage();
                String level = event.getLevel().name();
                int timestamp = (int)(System.currentTimeMillis() / 1000L);
                // Example: send to your API (use async/queued method in production)
                req.POST(buildBody(message, level, timestamp));
            }
        };
        customAppender.start();

        loggerConfig.addAppender(customAppender, null, null);
        ctx.updateLoggers(); // Apply changes
    }


    public static String buildBody(String message, String level, int timestamp){
		return "{\"message\":\"" + message + "\", \"level\":\"" + level + "\", \"timestamp\":" + timestamp + "}";
	}
}
