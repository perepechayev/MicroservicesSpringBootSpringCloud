package org.psp.api.core.event;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;

import java.time.ZonedDateTime;

public class Event<K, T> {
    public enum Type {
        CREATE,
        DELETE
    }

    private final Type eventType;
    private final K key;
    private final T data;
    private final ZonedDateTime eventCreatedAt;

    public Event() {
        eventType = null;
        key = null;
        data = null;
        eventCreatedAt = null;
    }

    public Event(Type eventType, K key, T data) {
        this.eventType = eventType;
        this.key = key;
        this.data = data;
        eventCreatedAt = ZonedDateTime.now();
    }

    public Type getEventType() {
        return eventType;
    }

    public K getKey() {
        return key;
    }

    public T getData() {
        return data;
    }

    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    public ZonedDateTime getEventCreatedAt() {
        return eventCreatedAt;
    }
}
