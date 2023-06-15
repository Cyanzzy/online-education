package com.cyan.springcloud.media.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author Cyan Chau
 * @create 2023-06-15
 */
@SpringBootTest
public class MediaFilesMapperTest {

    @Resource
    private MediaFilesMapper filesMapper;

    @Test
    public void testSelectById() {
        filesMapper.selectById(1580180577525002241L);
    }
}
