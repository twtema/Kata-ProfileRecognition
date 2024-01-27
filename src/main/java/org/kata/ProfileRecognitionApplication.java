package org.kata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class ProfileRecognitionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfileRecognitionApplication.class, args);
    }

}
