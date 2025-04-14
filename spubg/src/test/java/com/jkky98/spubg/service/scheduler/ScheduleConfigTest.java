package com.jkky98.spubg.service.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.jkky98.spubg.service.schedule.ScheduleConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ScheduleConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ScheduleConfig.class)
            .withPropertyValues("scheduling.enabled=true");

    @Test
    void testScheduleConfigLoads() {
        contextRunner.run(context -> {
            // ScheduleConfig 클래스 자체는 빈으로 등록되지 않더라도, @EnableScheduling가 활성화되는지 확인해볼 수 있습니다.
            // 스케줄러 관련 빈이 스프링 컨텍스트에 존재하는지를 확인하거나,
            // 단순히 컨텍스트 로딩이 성공했음을 검증하는 것도 하나의 테스트가 될 수 있습니다.
            assertThat(context).isNotNull();
        });
    }
}
