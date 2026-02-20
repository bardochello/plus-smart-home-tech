package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class SnapshotService {
    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        SensorsSnapshotAvro snapshot = snapshots.get(event.getHubId());
        if (snapshot == null) {
            snapshot = SensorsSnapshotAvro.newBuilder()
                    .setHubId(event.getHubId())
                    .setTimestamp(Instant.ofEpochMilli(event.getTimestamp()))
                    .setSensorsState(new HashMap<>())
                    .build();
            snapshots.put(event.getHubId(), snapshot);
            log.debug("Создан новый снапшот для хаба: {}", event.getHubId());
        }

        SensorStateAvro oldState = snapshot.getSensorsState().get(event.getId());

        if (oldState != null) {
            if (oldState.getTimestamp().toEpochMilli() > event.getTimestamp()) {
                log.trace("Событие устарело: sensor={}, eventTs={}, stateTs={}",
                        event.getId(), event.getTimestamp(), oldState.getTimestamp().toEpochMilli());
                return Optional.empty();
            }
            if (oldState.getData().equals(event.getPayload())) {
                log.trace("Данные не изменились для датчика: {}", event.getId());
                return Optional.empty();
            }
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(Instant.ofEpochMilli(event.getTimestamp()))
                .setData(event.getPayload())
                .build();

        snapshot.getSensorsState().put(event.getId(), newState);
        snapshot.setTimestamp(Instant.ofEpochMilli(event.getTimestamp()));

        log.debug("Снапшот обновлён: hub={}, sensor={}", event.getHubId(), event.getId());
        return Optional.of(snapshot);
    }
}
