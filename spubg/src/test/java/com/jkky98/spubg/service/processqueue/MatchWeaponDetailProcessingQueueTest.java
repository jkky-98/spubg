package com.jkky98.spubg.service.processqueue;

import com.jkky98.spubg.domain.MemberMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class MatchWeaponDetailProcessingQueueTest {

    @Mock
    MatchWeaponDetailQueueWorker matchWeaponDetailQueueWorker;

    @InjectMocks
    MatchWeaponDetailProcessingQueue processingQueue;

    @BeforeEach
    void setup() {
        // autoStartup 필드 주입
        ReflectionTestUtils.setField(processingQueue, "autoStartup", true);
    }

    @Test
    @DisplayName("[MatchWeaponDetailProcessingQueue][addMemberMatch] 정상 추가 및 사이즈 확인")
    void testAddMemberMatch() {
        MemberMatch memberMatch = MemberMatch.builder().build();
        processingQueue.addMemberMatch(memberMatch);

        assertThat(processingQueue.getQueueSize()).isEqualTo(1);
        assertThat(processingQueue.isQueueEmpty()).isFalse();
    }

    @Test
    @DisplayName("[MatchWeaponDetailProcessingQueue][isQueueEmpty] 초기 상태 확인")
    void testIsQueueEmptyInitially() {
        assertThat(processingQueue.isQueueEmpty()).isTrue();
        assertThat(processingQueue.getQueueSize()).isEqualTo(0);
    }

    @Test
    @DisplayName("[MatchWeaponDetailProcessingQueue][start] 워커 프로세스 N개 시작")
    void testStart() {
        processingQueue.start();

        assertThat(processingQueue.isRunning()).isTrue();
        verify(matchWeaponDetailQueueWorker, times(5))
                .process(any(BlockingQueue.class), any(AtomicBoolean.class));
    }

    @Test
    @DisplayName("[MatchWeaponDetailProcessingQueue][stop] 실행 중단")
    void testStop() {
        processingQueue.start();
        processingQueue.stop();

        assertThat(processingQueue.isRunning()).isFalse();
    }

    @Test
    @DisplayName("[MatchWeaponDetailProcessingQueue][getPhase] 값 확인")
    void testGetPhase() {
        assertThat(processingQueue.getPhase()).isEqualTo(1001);
    }

    @Test
    @DisplayName("[MatchWeaponDetailProcessingQueue][isAutoStartup] 프로퍼티 동작 확인")
    void testAutoStartup() {
        assertThat(processingQueue.isAutoStartup()).isTrue();

        // false로 바꿔보기
        ReflectionTestUtils.setField(processingQueue, "autoStartup", false);
        assertThat(processingQueue.isAutoStartup()).isFalse();
    }

    @Test
    @DisplayName("[MatchWeaponDetailProcessingQueue][addMemberMatch] InterruptedException 발생 시 정상 종료")
    void testAddMatchWithInterruptedException() throws Exception {
        // mock BlockingQueue
        BlockingQueue<MemberMatch> mockQueue = mock(BlockingQueue.class);
        doThrow(new InterruptedException("강제 인터럽트"))
                .when(mockQueue).put(any(MemberMatch.class));

        ReflectionTestUtils.setField(processingQueue, "queue", mockQueue);

        // when
        processingQueue.addMemberMatch(MemberMatch.builder().build());

        // catch 블럭 커버되면 테스트 성공
    }
}
