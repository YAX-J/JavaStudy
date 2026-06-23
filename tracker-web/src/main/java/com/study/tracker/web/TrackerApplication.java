package com.study.tracker.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.study.tracker")
@MapperScan("com.study.tracker.service.**.mapper")
public class TrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrackerApplication.class, args);
    }
}
