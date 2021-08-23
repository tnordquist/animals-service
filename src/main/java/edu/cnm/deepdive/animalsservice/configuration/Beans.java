package edu.cnm.deepdive.animalsservice.configuration;

import java.security.SecureRandom;
import java.util.Random;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Implements methods to satisfy dependencies on classes implemented outside of Spring and this
 * application. Using the {@code @Bean} annotation, the instances returned by these methods are made
 * available for injection into other classes.
 */
@Configuration
public class Beans {

  /**
   * Constructs and returns an instance of {@link Random} (or a suitable subclass),
   */
  @Bean
  public Random random() {
    return new SecureRandom();
  }

  /**
   * Constructs and returns an {@link ApplicationHome}, reflecting this application's runtime
   * location context.
   */
  @Bean
  public ApplicationHome applicationHome() {
    return new ApplicationHome(this.getClass());
  }

}
