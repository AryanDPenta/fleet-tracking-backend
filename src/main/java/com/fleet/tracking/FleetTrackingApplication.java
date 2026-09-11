// package com.fleet.tracking;

// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.scheduling.annotation.EnableScheduling;

// @SpringBootApplication
// @EnableScheduling
// public class FleetTrackingApplication {
//     public static void main(String[] args) {
//         SpringApplication.run(FleetTrackingApplication.class, args);
//     }
// }


package com.fleet.tracking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class FleetTrackingApplication {
    public static void main(String[] args) {
        // Force the JVM's clock to UTC regardless of the host machine's OS
        // timezone. Without this, LocalDateTime.now() (used for every
        // createdAt/recordedAt/startTime in the app) silently depends on
        // whatever timezone the server happens to be configured with -
        // consistent on a given host, but different between your local
        // machine and wherever you deploy, and ambiguous to the browser
        // either way since LocalDateTime carries no timezone info at all.
        // See JacksonConfig for the other half of this fix.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(FleetTrackingApplication.class, args);
    }
}