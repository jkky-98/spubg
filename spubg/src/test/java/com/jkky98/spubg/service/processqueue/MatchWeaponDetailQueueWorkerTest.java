package com.jkky98.spubg.service.processqueue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkky98.spubg.domain.MemberMatch;
import com.jkky98.spubg.pubg.request.TelemetryRequestBuilder;
import com.jkky98.spubg.service.business.MatchWeaponDetailSyncService;
import com.jkky98.spubg.service.implement.MemberMatchReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@EnableAsync
class MatchWeaponDetailQueueWorkerTest {

    @Mock
    MatchWeaponDetailSyncService matchWeaponDetailSyncService;

    @Mock
    ObjectProvider<TelemetryRequestBuilder> telemetryRequestBuilderProvider;

    @Mock
    TelemetryRequestBuilder telemetryRequestBuilder;

    @Mock
    MemberMatchReader memberMatchReader;

    @InjectMocks
    MatchWeaponDetailQueueWorker queueWorker;

    @Test
    @DisplayName("[MatchWeaponDetailQueueWorker][process] 정상 큐 처리 테스트")
    void testProcess() throws Exception {
        // given
        MemberMatch memberMatch = MemberMatch.builder()
                .id(42L)
                .build();

        BlockingQueue<MemberMatch> queue = new LinkedBlockingQueue<>(10);
        queue.put(memberMatch);
        AtomicBoolean running = new AtomicBoolean(true);

        String dummyTelemetryUrl = "https://telemetry.url/data";

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode dummyJson = mapper.createObjectNode();
        dummyJson.put("status", "ok");

        when(memberMatchReader.readAssetUrl(42L)).thenReturn(dummyTelemetryUrl);
        when(telemetryRequestBuilderProvider.getObject()).thenReturn(telemetryRequestBuilder);
        when(telemetryRequestBuilder.uri(dummyTelemetryUrl)).thenReturn(telemetryRequestBuilder);
        when(telemetryRequestBuilder.event(any())).thenReturn(telemetryRequestBuilder);
        when(telemetryRequestBuilder.execute()).thenReturn(dummyJson);
        doNothing().when(matchWeaponDetailSyncService).sync(42L, dummyJson);

        // when
        Thread workerThread = new Thread(() -> queueWorker.process(queue, running));
        workerThread.start();

        TimeUnit.MILLISECONDS.sleep(500);
        running.set(false);
        workerThread.interrupt();
        workerThread.join();

        // then
        verify(memberMatchReader).readAssetUrl(42L);
        verify(matchWeaponDetailSyncService).sync(eq(42L), eq(dummyJson));
        verify(telemetryRequestBuilder, atLeastOnce()).event(any());
        verify(telemetryRequestBuilder).uri(dummyTelemetryUrl);
        verify(telemetryRequestBuilder).execute();
    }

    @Test
    @DisplayName("[MatchWeaponDetailQueueWorker][process] InterruptedException 발생 시 안전 종료")
    void testProcess_withInterruptedException() throws Exception {
        // given
        BlockingQueue<MemberMatch> mockQueue = mock(BlockingQueue.class);
        when(mockQueue.poll(anyLong(), any())).thenThrow(new InterruptedException("강제 인터럽트"));

        AtomicBoolean running = new AtomicBoolean(true);

        // when
        Thread thread = new Thread(() -> queueWorker.process(mockQueue, running));
        thread.start();
        thread.join(); // 인터럽트 후 종료

        // then
        verifyNoInteractions(matchWeaponDetailSyncService);
        verifyNoInteractions(telemetryRequestBuilder);
        verifyNoInteractions(memberMatchReader);
    }
}
