package org.unitedinternet.cosmo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.orm.hibernate4.support.OpenSessionInViewFilter;
import org.springframework.web.filter.RequestContextFilter;
import org.unitedinternet.cosmo.filters.HttpLoggingFilter;
import org.unitedinternet.cosmo.servlet.SimpleDavDispatcherServlet;

import javax.servlet.Servlet;

/**
 * @author Kamill Sokol
 */
@ImportResource("applicationContext-cosmo.xml")
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, WebMvcAutoConfiguration.class})
public class CarldavApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarldavApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean openSessionInViewFilter() {
        final FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new OpenSessionInViewFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(2);
        return filterRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean requestContextFilter() {
        final FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new RequestContextFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(1);
        return filterRegistrationBean;
    }

    @Bean FilterRegistrationBean retryFilter() {
        final FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setName("retryFilter");
        filterRegistrationBean.setOrder(4);
        return filterRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean httpLoggingFilter() {
        final FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new HttpLoggingFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(3);
        return filterRegistrationBean;
    }

    @Bean
    public Servlet dispatcherServlet() {
        return new SimpleDavDispatcherServlet();
    }

}
