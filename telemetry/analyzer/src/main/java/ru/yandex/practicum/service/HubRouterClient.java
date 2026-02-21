package ru.yandex.practicum.service;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.model.Action;

import java.time.Instant;

@Slf4j
@Service
public class HubRouterClient {

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public void sendAction(String hubId, String scenarioName, String sensorId, Action action) {
        try {
            DeviceActionProto actionProto = DeviceActionProto.newBuilder()
                    .setSensorId(sensorId)
                    .setType(ActionTypeProto.valueOf(action.getType().name()))
                    .setValue(action.getValue() != null ? action.getValue() : 0)
                    .build();

            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(hubId)
                    .setScenarioName(scenarioName)
                    .setAction(actionProto)
                    .setTimestamp(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                    .build();

            hubRouterClient.handleDeviceAction(request);
            log.info("Action sent to hub: {}, scenario: {}, sensor: {}", hubId, scenarioName, sensorId);
        } catch (Exception e) {
            log.error("Failed to send action to Hub Router", e);
        }
    }
}
