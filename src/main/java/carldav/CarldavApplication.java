package carldav;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ImportResource;

import java.util.TimeZone;

@ImportResource("classpath:applicationContext-cosmo.xml")
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class CarldavApplication {

  static {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // required by hsqldb
    System.setProperty("file.encoding", "UTF-8");
  }

  public static void main(String[] args) {
    SpringApplication.run(CarldavApplication.class, args);
  }
}
