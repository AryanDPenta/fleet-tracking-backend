package com.fleet.tracking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FleetTrackingApplication {
    public static void main(String[] args) {
        SpringApplication.run(FleetTrackingApplication.class, args);
    }
}
