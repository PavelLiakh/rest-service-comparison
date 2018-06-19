package com.pliakh.restservicecomparison;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;


@Configuration
@ComponentScan(basePackages = "com.pliakh")
@SpringBootConfiguration
public class EntryPoint {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(EntryPoint.class);
        app.setBannerMode(Mode.OFF);

        Arrays.stream(args).forEach(arg -> {
            if ("--LOGLEVEL=DEBUG".equals(arg.toUpperCase())) {
                System.setProperty("LOG_LEVEL", "debug");
            }
        });

        app.run(args);
    }
}
