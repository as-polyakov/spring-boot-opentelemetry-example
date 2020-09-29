package com.otexample.client;

import com.otexample.OpenTelemetryConfigurer;
import com.otexample.metrics.EventManagerImpl;
import io.grpc.Context;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.TracingContextUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@SpringBootApplication(scanBasePackages = {"com.otexample.client"})
public class HelloWorldClient {

    private static final OpenTelemetryConfigurer otProxy = new OpenTelemetryConfigurer();


    public static void main(String[] args) {
        otProxy.setupJaegerExporter(HelloWorldClient.class.getName());
        new SpringApplicationBuilder(HelloWorldClient.class).web(WebApplicationType.NONE).run(args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
        return args -> {

            /**
             * here we _could_ just start span and let EventManager listener handle background event creation, but
             * this will not work becuase of lack of ContextChangeListener - we essentially need to intercept Context updates when new span becoimes
             * current since we gonna add event metadata to its CorrelationContext
             */
            try(Scope eventScope = EventManagerImpl.getInstance().startEvent("client-event", "apo", "123")) {
                Span s = otProxy.tracer.spanBuilder("Client span").startSpan();
                try(Scope scope = otProxy.tracer.withSpan(s)) {
                    restTemplate.setInterceptors(Arrays.asList((httpRequest, bytes, clientHttpRequestExecution) -> {
                        OpenTelemetry.getPropagators().getTextMapPropagator().inject(TracingContextUtils.withSpan(s, Context.current()),
                                httpRequest.getHeaders(), (httpHeaders, key, value) -> httpHeaders.set(key, value));
                        return clientHttpRequestExecution.execute(httpRequest, bytes);
                    }));
                    String response = restTemplate.getForObject(
                            "http://localhost:8080/hello/apolyakov", String.class);
                    System.out.println("Response: " + response);
                    s.end();
                }
            }
            Thread.sleep(1000);
            otProxy.shutdown();
        };
    }
}
