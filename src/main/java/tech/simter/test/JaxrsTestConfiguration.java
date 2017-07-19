package tech.simter.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author RJ
 */
@TestConfiguration
@ComponentScan(basePackages = {"tech.simter.genson.ext", "tech.simter.rest.jaxrs"})
public class JaxrsTestConfiguration {
}