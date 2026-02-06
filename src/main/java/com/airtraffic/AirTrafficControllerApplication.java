package com.airtraffic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AirTrafficControllerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AirTrafficControllerApplication.class, args);
    }
}
