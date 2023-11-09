package com.example.demo;

import brave.baggage.BaggageField;
import brave.baggage.CorrelationScopeConfig;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.CurrentTraceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}

@Configuration
class Config {
    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    BaggageField sessionIdField() {
        return BaggageField.create("x-session-id");
    }

    @Bean
    CurrentTraceContext.ScopeDecorator mdcScopeDecorator(BaggageField sessionIdField) {
        return MDCScopeDecorator.newBuilder()
                .clear()
                .add(CorrelationScopeConfig.SingleCorrelationField.newBuilder(sessionIdField).name("sessionId").flushOnUpdate().build())
                .build();
    }
}

@Slf4j
@RestController
@RequiredArgsConstructor
class Controller {

    private final RestTemplate restTemplate;
    private final BaggageField sessionIdField;

    @GetMapping
    public String hello() {
        log.info("Hello service called");
        log.info("Session id Baggage value is: {}", sessionIdField.getValue());

        restTemplate.getForObject("http://localhost:8080/hi", String.class);

        return "hello";
    }

    @GetMapping(path = "/hi")
    public String test() {
        log.info("Hi service called");

        return "hi";
    }
}