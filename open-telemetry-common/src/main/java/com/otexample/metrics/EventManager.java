package com.otexample.metrics;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.SpanProcessor;

public interface EventManager extends SpanProcessor {

    String EVENT_PREFIX = "event";

    Scope startEvent(String eventName, String userId, String tenantId);
    Scope startEventWithParent(String eventName, String userId, String tenantId, Context c);
}
