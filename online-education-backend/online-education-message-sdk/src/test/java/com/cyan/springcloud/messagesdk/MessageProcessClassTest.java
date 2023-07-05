package com.cyan.springcloud.messagesdk;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * MessageProcessClassTest
 *
 * @author Cyan Chau
 * @create 2023-07-05
 */
@SpringBootTest
public class MessageProcessClassTest {

    @Resource
    private MessageProcessClass messageProcessClass;

    @Test
    public void testExecute() throws Exception {
        System.out.println("开始执行-----》" + LocalDateTime.now());
        messageProcessClass.process(0, 1, "test", 5, 30);
        System.out.println("结束执行-----》" + LocalDateTime.now());
        Thread.sleep(9000000);

    }
}
