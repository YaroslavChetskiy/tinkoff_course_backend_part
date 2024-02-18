package edu.java.configuration;

import edu.java.scheduler.LinkUpdaterScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulerConfiguration {

    @Bean
    public LinkUpdaterScheduler linkUpdaterScheduler() {
        return new LinkUpdaterScheduler();
    }
}
