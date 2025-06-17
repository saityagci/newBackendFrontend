package com.sfaai.sfaai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;

@Configuration
@EnableJpaRepositories(basePackages = "com.sfaai.sfaai.repository")
@EnableTransactionManagement
@EnableJpaAuditing
public class JpaConfig {

    /**
     * Configures AuditorAware bean for JPA auditing
     * @return AuditorAware implementation
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        // This can be enhanced to return the current authenticated user
        return () -> Optional.of("system");
    }
}
