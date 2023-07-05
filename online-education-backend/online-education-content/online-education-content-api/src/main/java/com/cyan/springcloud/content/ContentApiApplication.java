package com.cyan.springcloud.content;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Cyan Chau
 * @create 2023-01-23
 */
@EnableSwagger2Doc
@SpringBootApplication(scanBasePackages = "com.cyan.springcloud.*")
@EnableFeignClients(basePackages = {"com.cyan.springcloud.content.feignclient"})
public class ContentApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApiApplication.class, args);
    }
}
