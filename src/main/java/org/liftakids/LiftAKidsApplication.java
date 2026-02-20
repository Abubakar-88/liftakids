package org.liftakids;

import org.liftakids.health.StartupHealthIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LiftAKidsApplication {
    @Autowired
    private StartupHealthIndicator startupHealthIndicator;

    public static void main(String[] args) {
        SpringApplication.run(LiftAKidsApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        // Mark as ready after everything is initialized
        startupHealthIndicator.markReady();
        System.out.println("=== APPLICATION FULLY READY ===");
        System.out.println("Health checks should now pass");
    }
}
