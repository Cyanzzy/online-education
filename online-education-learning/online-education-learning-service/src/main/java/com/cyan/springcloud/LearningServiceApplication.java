package com.cyan.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Cyan Chau
 * @create 2023-07-08
 */
@SpringBootApplication
//@EnableFeignClients("com.cyan.springcloud.learning.feignclient")
public class LearningServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearningServiceApplication.class, args);
    }
}
