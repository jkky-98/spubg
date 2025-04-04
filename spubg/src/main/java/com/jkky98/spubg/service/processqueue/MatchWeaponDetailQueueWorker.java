package com.jkky98.spubg.service.processqueue;

import com.fasterxml.jackson.databind.JsonNode;
import com.jkky98.spubg.domain.MemberMatch;
import com.jkky98.spubg.pubg.request.TelemetryRequestBuilder;
import com.jkky98.spubg.service.business.MatchWeaponDetailSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jkky98.spubg.pubg.enums.TelemetryEventType.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class MatchWeaponDetailQueueWorker {

    private final MatchWeaponDetailSyncService matchWeaponDetailSyncService;
    private final ObjectProvider<TelemetryRequestBuilder> telemetryRequestBuilder;

    @Async
    public void process(BlockingQueue<MemberMatch> queue, AtomicBoolean running) {
        while (running.get()) {
            try {
                MemberMatch memberMatch = queue.take();
                log.debug("[MatchWeaponDetailQueueWorker][process] 매치 공격 데이터 분석 시작 - MemberMatchId : {} ", memberMatch.getId());
                String telemetryUrl = memberMatch.getMatch().getAssetUrl();

                TelemetryRequestBuilder telemetryRequestBuilderPrototype = telemetryRequestBuilder.getObject();

                JsonNode rootNode = telemetryRequestBuilderPrototype
                        .uri(telemetryUrl)
                        .event(LOG_PLAYER_ATTACK)
                        .event(LOG_PLAYER_TAKE_DAMAGE)
                        .event(LOG_PLAYER_MAKE_GROGGY)
                        .execute();

                matchWeaponDetailSyncService.sync(memberMatch.getId(), rootNode);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[MatchWeaponDetailQueueWorker][process] 워커 쓰레드 인터럽트 발생", e);
                break;
            }
        }
    }
}
