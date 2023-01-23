package com.cyan.springcloud;

import com.cyan.springcloud.mapper.CourseBaseMapper;
import com.cyan.springcloud.model.po.CourseBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
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
