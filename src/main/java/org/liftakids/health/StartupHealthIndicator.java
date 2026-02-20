package org.liftakids.health;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
@Component
public class StartupHealthIndicator implements HealthIndicator {

    private final AtomicBoolean ready = new AtomicBoolean(false);

    @Override
    public Health health() {
        if (ready.get()) {
            return Health.up().withDetail("ready", true).build();
        }
        return Health.down().withDetail("ready", false).build();
    }

    public void markReady() {
        ready.set(true);
    }
}
