package tech.simter.test;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import tech.simter.genson.ext.data.PageConverterFactory;
import tech.simter.rest.jaxrs.CreatedStatusResponseFilter;
import tech.simter.rest.jaxrs.jersey.JerseyConfiguration;
import tech.simter.rest.jaxrs.jersey.JerseyResourceConfig;

/**
 * @author RJ
 */
@TestConfiguration
public class JaxrsTestConfiguration {
  @Bean
  @ConfigurationProperties(prefix = "simter.jersey")
  JerseyConfiguration jerseyConfiguration() {
    return new JerseyConfiguration();
  }

  @Bean
  JerseyResourceConfig jerseyResourceConfig() {
    return new JerseyResourceConfig();
  }

  @Bean
  CreatedStatusResponseFilter createdStatusResponseFilter() {
    return new CreatedStatusResponseFilter();
  }

  @Bean
  PageConverterFactory pageConverterFactory() {
    return new PageConverterFactory();
  }
}