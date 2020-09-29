package com.otexample;

import com.google.auto.value.AutoValue;
import com.otexample.metrics.EventManager;

@AutoValue
public abstract class Event {

    public abstract String eventId();
    public abstract String userId();
    public abstract String tenantId();


    public static Builder builder() {
        return new AutoValue_Event.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder eventId(String eventId);
        public abstract Builder userId(String userId);
        public abstract Builder tenantId(String tenantId);

        public abstract Event build();

    }

    public static String asAttributeKey(String name) {
        return EventManager.EVENT_PREFIX + "." + name;
    }

}
