package com.cyan.springcloud;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Redis Auth Test
 *
 * @author Cyan Chau
 * @create 2023-11-15
 */
@SpringBootTest
public class RedisAuthTest {

    @Value("${spring.redis.password}")
    private String password;

    @Test
    public void testGetPassword() {

        System.out.println(password);
    }
}
