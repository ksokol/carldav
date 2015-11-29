package util;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import carldav.service.time.TimeService;
import carldav.service.generator.IdGenerator;

/**
 * @author Kamill Sokol
 */
public class MockBeans {

    @Bean
    public TimeService timeService() {
        return mock(TimeService.class);
    }

    @Bean
    public IdGenerator idGenerator() {
        return mock(IdGenerator.class);
    }
}
