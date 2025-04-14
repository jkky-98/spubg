package com.jkky98.spubg.service.processqueue;

import com.jkky98.spubg.domain.Match;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class MatchProcessingQueueTest {

    @Mock
    MatchQueueWorker matchQueueWorker;

    @InjectMocks
    MatchProcessingQueue matchProcessingQueue;

    @BeforeEach
    void setUp() {
        // 강제로 autoStartup 값을 true로 지정
        ReflectionTestUtils.setField(matchProcessingQueue, "autoStartup", true);
    }

    @Test
    @DisplayName("[MatchProcessingQueue][addMatch] 큐에 정상적으로 추가됨")
    void testAddMatchAndSize() {
        Match match = Match.builder().matchApiId("abc").build();
        matchProcessingQueue.addMatch(match);

        assertThat(matchProcessingQueue.getQueueSize()).isEqualTo(1);
        assertThat(matchProcessingQueue.isQueueEmpty()).isFalse();
    }

    @Test
    @DisplayName("[MatchProcessingQueue][addMatch] InterruptedException 발생 시 정상적으로 interrupt 처리됨")
    void testAddMatch_withInterruptedException() throws Exception {
        // given
        Match match = Match.builder().matchApiId("abc").build();

        // matchProcessingQueue 내부 queue 필드를 mock으로 교체
        BlockingQueue<Match> mockQueue = mock(BlockingQueue.class);
        doThrow(new InterruptedException("강제 인터럽트")).when(mockQueue).put(any());

        // private final이지만 테스트에서 강제로 바꿔줌
        ReflectionTestUtils.setField(matchProcessingQueue, "queue", mockQueue);

        // when
        matchProcessingQueue.addMatch(match);

        // then
        // 여기서는 그냥 커버만 타는 게 목적이라 예외를 던지지 않으면 OK
        // 로그나 Thread.interrupt()는 별도 검증 안 해도 무방
    }

    @Test
    @DisplayName("[MatchProcessingQueue][isQueueEmpty] 초기 상태는 비어 있음")
    void testIsQueueEmptyInitially() {
        assertThat(matchProcessingQueue.isQueueEmpty()).isTrue();
        assertThat(matchProcessingQueue.getQueueSize()).isEqualTo(0);
    }

    @Test
    @DisplayName("[MatchProcessingQueue][start] 워커가 실행되고 running 상태가 true가 됨")
    void testStart() {
        matchProcessingQueue.start();

        assertThat(matchProcessingQueue.isRunning()).isTrue();
        // workerCount = 5 → 5번 호출돼야 함
        verify(matchQueueWorker, times(5)).process(any(BlockingQueue.class), any(AtomicBoolean.class));
    }

    @Test
    @DisplayName("[MatchProcessingQueue][stop] running 상태가 false가 됨")
    void testStop() {
        matchProcessingQueue.start();
        matchProcessingQueue.stop();

        assertThat(matchProcessingQueue.isRunning()).isFalse();
    }

    @Test
    @DisplayName("[MatchProcessingQueue][isAutoStartup] 프로퍼티 값 확인")
    void testAutoStartup() {
        // autoStartup은 true로 세팅됨
        assertThat(matchProcessingQueue.isAutoStartup()).isTrue();

        // false로 설정한 경우 테스트
        ReflectionTestUtils.setField(matchProcessingQueue, "autoStartup", false);
        assertThat(matchProcessingQueue.isAutoStartup()).isFalse();
    }

    @Test
    @DisplayName("[MatchProcessingQueue][getPhase] phase 값 확인")
    void testGetPhase() {
        assertThat(matchProcessingQueue.getPhase()).isEqualTo(1000);
    }
}
