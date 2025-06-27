package com.sfaai.sfaai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootApplication
@EnableScheduling
@EnableWebSecurity
public class SfaAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SfaAiApplication.class, args);
    }


}
