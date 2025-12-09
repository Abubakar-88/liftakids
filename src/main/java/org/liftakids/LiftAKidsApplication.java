package org.liftakids;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LiftAKidsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiftAKidsApplication.class, args);
    }

}
