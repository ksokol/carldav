package org.unitedinternet.cosmo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.Servlet;
import java.util.TimeZone;

/**
 * @author Kamill Sokol
 */
@ComponentScan("carldav.controller")
@ImportResource("applicationContext-cosmo.xml")
@SpringBootApplication
public class CarldavApplication {

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // required by hsqldb
        System.setProperty("file.encoding","UTF-8");
    }

    public static void main(String[] args) {
        SpringApplication.run(CarldavApplication.class, args);
    }

    @Bean
    public Servlet dispatcherServlet() {
        final DispatcherServlet dispatcherServlet = new DispatcherServlet();
        dispatcherServlet.setDispatchOptionsRequest(true);
        return dispatcherServlet;
    }

}
