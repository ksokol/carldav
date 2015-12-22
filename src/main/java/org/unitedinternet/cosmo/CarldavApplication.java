package org.unitedinternet.cosmo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.context.embedded.DelegatingFilterProxyRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.unitedinternet.cosmo.servlet.SimpleDavDispatcherServlet;

import java.util.TimeZone;
import javax.servlet.Servlet;

/**
 * @author Kamill Sokol
 */
@ImportResource("applicationContext-cosmo.xml")
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, WebMvcAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
public class CarldavApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // required by hsqldb
        SpringApplication.run(CarldavApplication.class, args);
    }

    @Bean
    public DelegatingFilterProxyRegistrationBean securityFilterChainRegistration() {
        DelegatingFilterProxyRegistrationBean registration = new DelegatingFilterProxyRegistrationBean(AbstractSecurityWebApplicationInitializer.DEFAULT_FILTER_NAME);
        return registration;
    }

    @Bean
    public Servlet dispatcherServlet() {
        return new SimpleDavDispatcherServlet();
    }

}
