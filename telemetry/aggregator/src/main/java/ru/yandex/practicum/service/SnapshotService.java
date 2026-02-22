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
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SnapshotService {
    // Храним последние известные состояния хабов
    private final Map<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();
        Instant eventTimestamp = Instant.ofEpochMilli(event.getTimestamp());

        SensorsSnapshotAvro oldSnapshot = snapshots.getOrDefault(hubId, SensorsSnapshotAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(eventTimestamp)
                .setSensorsState(new HashMap<>())
                .build());

        Map<String, SensorStateAvro> newStateMap = new HashMap<>(oldSnapshot.getSensorsState());

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(eventTimestamp)
                .setData(event.getPayload())
                .build();
        newStateMap.put(sensorId, newState);

        Instant snapshotTimestamp = eventTimestamp;

        SensorsSnapshotAvro newSnapshot = SensorsSnapshotAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(snapshotTimestamp)
                .setSensorsState(newStateMap)
                .build();

        if (newSnapshot.equals(oldSnapshot)) {
            log.debug("Нет изменений в снапшоте для хаба {}, пропускаем публикацию", hubId);
            return Optional.empty();
        } else {
            snapshots.put(hubId, newSnapshot);
            log.info("Обновлен снапшот для хаба {} с датчиком {}, новый timestamp {}", hubId, sensorId, snapshotTimestamp);
            return Optional.of(newSnapshot);
        }
    }
}
