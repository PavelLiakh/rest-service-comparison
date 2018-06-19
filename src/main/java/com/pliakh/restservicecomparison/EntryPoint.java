package com.pliakh.restservicecomparison;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.pliakh")
@SpringBootConfiguration
public class EntryPoint {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(EntryPoint.class);
        app.setBannerMode(Mode.OFF);
        app.run(args);
    }
}
