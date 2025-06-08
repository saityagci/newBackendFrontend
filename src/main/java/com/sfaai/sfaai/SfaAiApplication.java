package com.sfaai.sfaai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class SfaAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SfaAiApplication.class, args);

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            System.out.println(encoder.encode("123"));

    }


}
