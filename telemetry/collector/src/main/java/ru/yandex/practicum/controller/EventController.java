package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;
import ru.yandex.practicum.service.hub.HubEventProcessingService;
import ru.yandex.practicum.service.sensor.SensorEventProcessingService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final SensorEventProcessingService sensorEventProcessingService;
    private final HubEventProcessingService hubEventProcessingService;

    @PostMapping("/sensors")
    public ResponseEntity<Void> collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        log.info("Получено событие датчика: {}", event);
        sensorEventProcessingService.process(event);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/hubs")
    public ResponseEntity<Void> collectHubEvent(@Valid @RequestBody HubEvent event) {
        log.info("Получено событие хаба: {}", event);
        hubEventProcessingService.process(event);
        return ResponseEntity.ok().build();
    }
}