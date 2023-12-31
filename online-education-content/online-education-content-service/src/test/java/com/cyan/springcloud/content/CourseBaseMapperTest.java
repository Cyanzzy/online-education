package com.cyan.springcloud.content;

import com.cyan.springcloud.content.mapper.CourseBaseMapper;
import com.cyan.springcloud.model.po.CourseBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * CourseBaseMapperTest
 *
 * @author Cyan Chau
 * @create 2023-01-23
 */
@SpringBootTest
public class CourseBaseMapperTest {

    @Resource
    private CourseBaseMapper courseBaseMapper;

    @Test
    void testCourseBaseMapper() {
        CourseBase courseBase = courseBaseMapper.selectById(71L);
        Assertions.assertNotNull(courseBase);
    }

}
