package org.kata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ProfileRecognitionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfileRecognitionApplication.class, args);
    }

}
