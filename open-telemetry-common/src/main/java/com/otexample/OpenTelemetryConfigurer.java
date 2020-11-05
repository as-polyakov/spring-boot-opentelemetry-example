package com.otexample;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.OpenTelemetry;
/*
import io.opentelemetry.exporters.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporters.logging.LoggingMetricExporter;
*/
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.export.IntervalMetricReader;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracerProvider;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class OpenTelemetryConfigurer {

/*
    private String ip = "192.168.99.101";
    private int port = 14250;

    public TracerProvider tracerProvider = OpenTelemetry.getTracerProvider();
    public Tracer tracer = tracerProvider.get("io.opentelemetry.example.JaegerExample");
    // Export traces to Jaeger
    private JaegerGrpcSpanExporter jaegerExporter;
    final IntervalMetricReader intervalMetricReader;

    public OpenTelemetryConfigurer() {
        intervalMetricReader = IntervalMetricReader.builder()
                .setExportIntervalMillis(1000)
                .setMetricProducers(Collections.singletonList(OpenTelemetrySdk.getMeterProvider().getMetricProducer()))
                .setMetricExporter(new LoggingMetricExporter()).build();
    }

    public void setupJaegerExporter(String serviceName) {
        // Create a channel towards Jaeger end point
        ManagedChannel jaegerChannel =
                ManagedChannelBuilder.forAddress(ip, port).usePlaintext().build();
        // Export traces to Jaeger
        this.jaegerExporter =
                JaegerGrpcSpanExporter.newBuilder()
                        .setServiceName(serviceName)
                        .setChannel(jaegerChannel)
                        .setDeadlineMs(30000)
                        .build();

        // Set to process the spans by the Jaeger Exporter
        OpenTelemetrySdk.getTracerProvider().addSpanProcessor(SimpleSpanProcessor.newBuilder(this.jaegerExporter).build());
    }


    // graceful shutdown
    public void shutdown() {
        OpenTelemetrySdk.getTracerProvider().shutdown();
        intervalMetricReader.shutdown();
    }
    */
}
