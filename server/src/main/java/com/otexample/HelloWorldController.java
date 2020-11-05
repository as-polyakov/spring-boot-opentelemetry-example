package com.otexample;

import com.atlassian.obsvs.Initializer;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.metrics.LongValueRecorder;
import io.opentelemetry.metrics.Meter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;

@Controller
@SpringBootApplication
@Configuration
public class HelloWorldController {

    @PostConstruct
    public void initObservasaurus() {
        Initializer.init();
    }


    @GetMapping("/hello/{userId}")
    @ResponseBody
    public String sayHello(@PathVariable String userId) throws InterruptedException {
        long l = System.currentTimeMillis();
        Meter m = OpenTelemetry.getMeter("apo");
        LongValueRecorder latencyRecorder = m.longValueRecorderBuilder("http.latency")
                .build();
        Thread.sleep(100);
        latencyRecorder.record(System.currentTimeMillis() - l);
        return "Hello, " + userId + "!\n";
    }

    /**
     * make sure to setup environment variables for your app:
     * export OTEL_RESOURCE_ATTRIBUTES="service_id=myServer,service.version=v1.2.3"
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(HelloWorldController.class, args);
    }
}
