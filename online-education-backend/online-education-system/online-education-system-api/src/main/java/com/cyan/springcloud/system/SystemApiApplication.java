package com.cyan.springcloud.system;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 系统管理
 *
 * @author Cyan Chau
 * @create 2023-01-26
 */
@EnableScheduling
@EnableSwagger2Doc
@SpringBootApplication
public class SystemApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemApiApplication.class, args);
    }
}
