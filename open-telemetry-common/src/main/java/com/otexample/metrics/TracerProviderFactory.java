package com.otexample.metrics;

import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.trace.TracerProvider;

public class TracerProviderFactory implements io.opentelemetry.trace.spi.TracerProviderFactory {

    @Override
    public TracerProvider create() {
        TracerSdkProvider tracerSdkProvider = TracerSdkProvider.builder().build();
        EventManager instance = EventManagerImpl.getInstance();
        tracerSdkProvider.addSpanProcessor(instance);
        return tracerSdkProvider;
    }

}
