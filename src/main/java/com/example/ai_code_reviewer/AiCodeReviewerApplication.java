package com.example.ai_code_reviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.ai_code_reviewer", "com.yourorg.aicode"})
@EntityScan(basePackages = {"com.example.ai_code_reviewer", "com.yourorg.aicode.model"})
@EnableJpaRepositories(basePackages = {"com.example.ai_code_reviewer", "com.yourorg.aicode.repository"})
public class AiCodeReviewerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCodeReviewerApplication.class, args);
    }

}
