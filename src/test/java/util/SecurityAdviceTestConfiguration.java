package util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.unitedinternet.cosmo.security.aop.SecurityAdvice;
import org.unitedinternet.cosmo.security.mock.MockSecurityManager;

/**
 * @author Kamill Sokol
 */
@Configuration
public class SecurityAdviceTestConfiguration {

    @Bean
    public SecurityAdvice securityAdvice() {
        return new SecurityAdvice(new MockSecurityManager());
    }

}
