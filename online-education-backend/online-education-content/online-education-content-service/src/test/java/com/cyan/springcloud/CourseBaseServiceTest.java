package com.cyan.springcloud;

import com.cyan.springcloud.base.model.PageParams;
import com.cyan.springcloud.base.model.PageResult;
import com.cyan.springcloud.model.dto.QueryCourseParamsDto;
import com.cyan.springcloud.model.po.CourseBase;
import com.cyan.springcloud.content.service.CourseBaseService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author Cyan Chau
 * @create 2023-01-26
 */
@SpringBootTest
public class CourseBaseServiceTest {

    @Resource
    private CourseBaseService courseBaseService;

    @Test
    public void testCourseBaseService() {
        PageParams pageParams = new PageParams();
        PageResult<CourseBase> result = courseBaseService.queryCourseBaseList(pageParams, new QueryCourseParamsDto());
        System.out.println(result);
    }
}
