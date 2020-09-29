package com.otexample.metrics;

import com.google.common.collect.ImmutableSet;
import io.opentelemetry.metrics.MeterProvider;
import io.opentelemetry.sdk.metrics.MeterSdkProvider;

public class MeterProviderFactory implements io.opentelemetry.metrics.spi.MeterProviderFactory {

    @Override
    public MeterProvider create() {
        return MeterSdkProvider.builder().setStickyLabelsPrefixes(ImmutableSet.of(EventManager.EVENT_PREFIX)).build();
    }

}
