package edu.java.configuration;

import edu.java.scheduler.LinkUpdaterScheduler;
import edu.java.service.LinkUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SchedulerConfiguration {

    private final LinkUpdater linkUpdater;

    @Bean
    public LinkUpdaterScheduler linkUpdaterScheduler() {
        return new LinkUpdaterScheduler(linkUpdater);
    }
}
