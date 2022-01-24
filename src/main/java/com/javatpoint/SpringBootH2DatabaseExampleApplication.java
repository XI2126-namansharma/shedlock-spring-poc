package com.javatpoint;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SchedulerLock;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.ScheduledLockConfiguration;
import net.javacrumbs.shedlock.spring.ScheduledLockConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
public class SpringBootH2DatabaseExampleApplication {

    private static final Logger logger = LoggerFactory.getLogger(SpringBootH2DatabaseExampleApplication.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootH2DatabaseExampleApplication.class, args);
    }

    @Configuration
    public static class SchedulingConf implements SchedulingConfigurer {

        @Bean
        public TaskScheduler taskScheduler() {
            return new ConcurrentTaskScheduler();
        }

        @Override
        public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
            taskRegistrar.setTaskScheduler(taskScheduler());
        }
    }


    @Configuration
    public static class Conf {

        @Bean
        LockProvider lockProvider(DataSource dataSource) {
            return new JdbcTemplateLockProvider(dataSource);
        }

        @Bean
        public ScheduledLockConfiguration shedLockConfig(
                LockProvider lockProvider,
                TaskScheduler taskScheduler) {
            return ScheduledLockConfigurationBuilder
                    .withLockProvider(lockProvider)
                    .withTaskScheduler(taskScheduler)
                    .withDefaultLockAtMostFor(Duration.ofMinutes(10))
                    .build();
        }
    }


    @Service
    public class ScheduledService {

        @Scheduled(cron = "0/5 * * * * *")
        @SchedulerLock(name = "testThis")
        public void callSomething() {
            List<Map<String, Object>> mapList = jdbcTemplate.queryForList("select * from STUDENT");
            if (!mapList.isEmpty()) {
                mapList.forEach(record -> logger.info("Record: {}", record));
            } else {
                logger.error("Something called");
            }
        }
    }
}
