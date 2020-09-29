package com.otexample.metrics;

import com.otexample.Event;
import io.grpc.Context;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.correlationcontext.EntryMetadata;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.TracingContextUtils;
import org.apache.maven.shared.utils.StringUtils;

public class EventManagerImpl implements EventManager {

    private final ThreadLocal<Scope> currentEventScope = new ThreadLocal<>();
    private final ThreadLocal<Event> currentEvent = new ThreadLocal<>();

    private static class LazyHolder {
        static final EventManagerImpl INSTANCE = new EventManagerImpl();
    }

    public static EventManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public EventManagerImpl() {
    }

    @Override
    public void onStart(ReadWriteSpan span) {
        if (!isEventActive()) {
            Event e = makeEventFromSpan(span);
            setCurrentEvent(e, injectEventIntoCorrelationContext(e));
        }
        injectEventIntoSpan(span);
    }

    private void setCurrentEvent(Event e, Scope s) {
        currentEvent.set(e);
        currentEventScope.set(s);
    }

    private void injectEventIntoSpan(ReadWriteSpan span) {
        CorrelationContext ctx = OpenTelemetry.getCorrelationContextManager().getCurrentContext();
        ctx.getEntries().stream().filter(e -> e.getKey().startsWith(EVENT_PREFIX)).forEach(e -> span.setAttribute(e.getKey(), e.getValue()));
    }

    private Scope injectEventIntoCorrelationContext(Event e) {
        CorrelationContext.Builder contextBuilder = OpenTelemetry.getCorrelationContextManager().contextBuilder();

        return OpenTelemetry.getCorrelationContextManager().withContext(

                contextBuilder.put(EVENT_PREFIX + "." + "name", e.eventId(), EntryMetadata.create(EntryMetadata.EntryTtl.UNLIMITED_PROPAGATION))
                        .put(EVENT_PREFIX + "." + "tenantId", e.tenantId(), EntryMetadata.create(EntryMetadata.EntryTtl.UNLIMITED_PROPAGATION))
                        .put(EVENT_PREFIX + "." + "userId", e.userId(), EntryMetadata.create(EntryMetadata.EntryTtl.UNLIMITED_PROPAGATION))
                        .setParent(OpenTelemetry.getCorrelationContextManager().getCurrentContext()).build());
    }

    private Event makeEventFromSpan(ReadWriteSpan span) {
        return Event.builder().eventId(span.getName()).userId("").tenantId("").build();
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {
        Event event = currentEvent.get();
        if (event != null && event.eventId().equals(span.getName())) {
            currentEventScope.get().close();
        }
    }

    @Override
    public boolean isEndRequired() {
        return true;
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode forceFlush() {
        return null;
    }

    @Override
    public Scope startEvent(String eventName, String userId, String tenantId) {
        return startEventWithParent(eventName, userId, tenantId, Context.current());
    }

    @Override
    public Scope startEventWithParent(String eventName, String userId, String tenantId, Context c) {
        Scope eventScope = injectEventIntoCorrelationContext(Event.builder().eventId(eventName).userId(userId).tenantId(tenantId).build());
        Span.Builder spanBuilder = OpenTelemetry.getTracer("observasaurus").spanBuilder(eventName).setParent(TracingContextUtils.getSpan(c));
        Span span = spanBuilder.startSpan();
        Scope spanScope = OpenTelemetry.getTracer("observasaurus").withSpan(span);
        return () -> {
            span.end();
            spanScope.close();
            eventScope.close();
        };
    }

    private boolean isEventActive() {
        return !StringUtils.isEmpty(OpenTelemetry.getCorrelationContextManager().getCurrentContext().getEntryValue(EVENT_PREFIX + "." + "name"));
    }
}
