package com.cyan.springcloud.search;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Cyan Chau
 * @create 2023-07-06
 */
@SpringBootTest
public class GetValueTest {

    @Value("${elasticsearch.hostlist}")
    private String hostlist;

    @Value("${elasticsearch.course.index}")
    private String courseIndexStore;

    @Value("${elasticsearch.course.source_fields}")
    private String sourceFields;

    @Test
    public void testGetValue() {
        System.out.println(hostlist);
        System.out.println(courseIndexStore);
        System.out.println(sourceFields);
    }
}
