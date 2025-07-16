package com.photoshop.fotoshop.image;


import com.samskivert.mustache.Mustache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

@Configuration
public class MustacheConfig {
    @Bean
    public Mustache.Compiler mustacheCompiler(
            Mustache.TemplateLoader templateLoader) {

        return Mustache.compiler()
                .defaultValue("")
                .withFormatter(value -> {
                    if (value instanceof TemporalAccessor) {
                        return DateTimeFormatter.ofPattern("MMM d, yyyy").format((TemporalAccessor) value);
                    }
                    return String.valueOf(value);
                })
                .withLoader(templateLoader);
    }
}
