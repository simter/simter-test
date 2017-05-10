package tech.simter.rest.jaxrs.jersey;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import tech.simter.genson.ext.data.PageConverterFactory;
import tech.simter.rest.jaxrs.CreatedStatusResponseFilter;

/**
 * @author RJ
 */
@Configuration
@ComponentScan(basePackageClasses = {JerseyResourceConfig.class, CreatedStatusResponseFilter.class, PageConverterFactory.class})
public class JerseyConfiguration4Import {
}
