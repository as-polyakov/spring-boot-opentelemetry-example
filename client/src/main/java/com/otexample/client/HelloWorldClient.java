package com.otexample.client;

import com.atlassian.obsvs.Initializer;
import com.atlassian.obsvs.event.EventManagerImpl;
import com.atlassian.obsvs.event.ObsvsEventCallerMetadata;
import com.atlassian.obsvs.event.ObsvsEventInput;
import com.otexample.OpenTelemetryConfigurer;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@SpringBootApplication(scanBasePackages = {"com.otexample.client"})
public class HelloWorldClient {

    private static final OpenTelemetryConfigurer otProxy = new OpenTelemetryConfigurer();

    /**
     * make sure to setup environment variables for your app:
     * export OTEL_RESOURCE_ATTRIBUTES="service_id=myClient,service.version=v1.2.3"
     * @param args
     */
    public static void main(String[] args) {
        Initializer.init();
        new SpringApplicationBuilder(HelloWorldClient.class).web(WebApplicationType.NONE).run(args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        builder.interceptors(Collections.singletonList((httpRequest, bytes, clientHttpRequestExecution) -> {
            OpenTelemetry.getPropagators().getTextMapPropagator().<HttpHeaders>inject(Context.current(), httpRequest.getHeaders(), HttpHeaders::set);
            return clientHttpRequestExecution.execute(httpRequest, bytes);
        }));
        return builder.build();
    }

    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
        return args -> {

            try (Scope ignored = EventManagerImpl.getInstance().startEvent(ObsvsEventInput.builder()
                    .spanBuilder(OpenTelemetry.getTracer("example").spanBuilder("client-event"))
                    .eventCallerMetadata(ObsvsEventCallerMetadata.builder()
                            .userId("test-user")
                            .eventName("myEvent")
                            .build())
                    .build())) {
                String response = restTemplate.getForObject(
                        "http://localhost:8080/hello/userName", String.class);
                System.out.println("Response: " + response);
            }
            Thread.sleep(1000);
        };
    }
}
