package com.jkky98.spubg.service.processqueue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jkky98.spubg.domain.Match;
import com.jkky98.spubg.pubg.request.PubgApiRequestService;
import com.jkky98.spubg.service.business.MatchSyncService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@EnableAsync
public class MatchQueueWorkerTest {

    @Mock
    private PubgApiRequestService pubgApiRequestService;

    @Mock
    private MatchSyncService matchSyncService;

    @InjectMocks
    private MatchQueueWorker matchQueueWorker;

    @Test
    @DisplayName("[MatchQueueWorker][process] 큐 처리 및 동기화 테스트")
    void testProcess() throws InterruptedException {
        // given
        Match match = Match.builder()
                .id(1L)
                .matchApiId("matchApiId")
                .build();

        BlockingQueue<Match> queue = new LinkedBlockingQueue<>(10);
        queue.put(match);
        AtomicBoolean running = new AtomicBoolean(true);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode dummyJson = mapper.createObjectNode();
        dummyJson.put("dummy", "value");

        when(pubgApiRequestService.requestMatch("matchApiId")).thenReturn(dummyJson);
        doNothing().when(matchSyncService).sync(anyLong(), any(JsonNode.class));

        // when
        Thread workerThread = new Thread(() -> matchQueueWorker.process(queue, running));
        workerThread.start();

        TimeUnit.MILLISECONDS.sleep(500);
        running.set(false);
        workerThread.interrupt();
        workerThread.join();

        // then
        verify(pubgApiRequestService, times(1)).requestMatch("matchApiId");

        ArgumentCaptor<Long> matchIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<JsonNode> jsonCaptor = ArgumentCaptor.forClass(JsonNode.class);

        verify(matchSyncService, times(1)).sync(matchIdCaptor.capture(), jsonCaptor.capture());

        assertThat(matchIdCaptor.getValue()).isEqualTo(1L);
        assertThat(jsonCaptor.getValue().get("dummy").asText()).isEqualTo("value");
    }

    @Test
    @DisplayName("[MatchQueueWorker][process] InterruptedException 발생 시 안전 종료 테스트")
    void testProcess_withInterruptedException() throws InterruptedException {
        // given
        BlockingQueue<Match> mockQueue = mock(BlockingQueue.class);
        when(mockQueue.poll(anyLong(), any())).thenThrow(new InterruptedException("강제 인터럽트"));

        AtomicBoolean running = new AtomicBoolean(true);

        // when
        Thread workerThread = new Thread(() -> matchQueueWorker.process(mockQueue, running));
        workerThread.start();
        workerThread.join();

        // then
        verify(pubgApiRequestService, never()).requestMatch(anyString());
        verify(matchSyncService, never()).sync(anyLong(), any(JsonNode.class));
    }

}
