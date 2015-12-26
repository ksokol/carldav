package util;

import static org.mockito.Mockito.mock;

import carldav.service.time.TimeService;
import org.springframework.context.annotation.Bean;

/**
 * @author Kamill Sokol
 */
public class MockBeans {

    @Bean
    public TimeService timeService() {
        return mock(TimeService.class);
    }
}
