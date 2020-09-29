package com.otexample;

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

@Controller
@SpringBootApplication
@Configuration
public class HelloWorldController {

    @Autowired
    private OpenTelemetryConfigurer otProxy;

    @Bean
    public OpenTelemetryConfigurer otProxy() {
        OpenTelemetryConfigurer otProxy = new OpenTelemetryConfigurer();
        otProxy.setupJaegerExporter(HelloWorldController.class.getName());
        return otProxy;
    }


    @GetMapping("/hello/{userId}")
    @ResponseBody
    public String sayHello(@PathVariable String userId) throws InterruptedException {
        Meter m = OpenTelemetry.getMeter("apo");
        LongValueRecorder latencyRecorder = m.longValueRecorderBuilder("http.latency")
                .build();
        latencyRecorder.record(10);
        Thread.sleep(100);
        return "Hello, " + userId + "!\n";
    }

    public static void main(String[] args) throws Exception {


        SpringApplication.run(HelloWorldController.class, args);
    }
}
