package com.cyan.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Cyan Chau
 * @create 2023-07-06
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.cyan.springcloud.ucenter.feignclient")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

}