package ru.yandex.practicum.service.sensor;

import ru.yandex.practicum.model.sensor.SensorEvent;

public interface SensorEventService {
    void process(SensorEvent event);

    boolean supports(String eventType);
}