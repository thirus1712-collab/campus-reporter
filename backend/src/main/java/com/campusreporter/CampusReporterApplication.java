package com.campusreporter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CampusReporterApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusReporterApplication.class, args);
    }
}
