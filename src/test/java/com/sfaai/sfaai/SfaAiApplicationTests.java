package com.sfaai.sfaai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "jwt.secret=test_secret_key_that_is_at_least_256_bits_long_for_testing_purposes_only",
    "vapi.api.key=test-vapi-key-mock",
    "elevenlabs.api.key=test-elevenlabs-key-mock",
    "spring.main.allow-bean-definition-overriding=true"
})
class SfaAiApplicationTests {

    @Test
    void contextLoads() {
    }

}
