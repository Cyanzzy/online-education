package com.cyan.springcloud.content;

import com.cyan.springcloud.base.model.PageParams;
import com.cyan.springcloud.base.model.PageResult;
import com.cyan.springcloud.model.dto.AddCourseDto;
import com.cyan.springcloud.model.dto.CourseBaseInfoDto;
import com.cyan.springcloud.model.dto.QueryCourseParamsDto;
import com.cyan.springcloud.model.po.CourseBase;
import com.cyan.springcloud.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * CourseBaseServiceTest
 *
 * @author Cyan Chau
 * @create 2023-01-26
 */
@SpringBootTest
public class CourseBaseServiceTest {

    @Resource
    private CourseBaseInfoService courseBaseService;

    @Test
    public void testCourseBaseService() {
        PageParams pageParams = new PageParams();
        PageResult<CourseBase> result = courseBaseService.queryCourseBaseList(pageParams, new QueryCourseParamsDto());
        System.out.println(result);
    }

    @Test
    public void testGetCourseBaseInfo() {
        Long id = 25L;
        CourseBaseInfoDto courseBaseInfo = courseBaseService.getCourseBaseInfo(id);
        System.out.println(courseBaseInfo);
        System.out.println(courseBaseInfo.getName());
    }

    @Test
    public void testCreateCourseBase() {
//        Long companyId = 22L;
//        AddCourseDto dto = new AddCourseDto();
//        dto.setCharge("201001");
//        dto.setOriginalPrice(200);
//
//        "charge": "",
//                "price": 10,
//                "originalPrice":200,
//                "qq": "22333",
//                "wechat": "223344",
//                "phone": "13333333",
//                "validDays": 365,
//                "mt": "1-1",
//                "st": "1-1-1",
//                "name": "Java编程",
//                "pic": "132132",
//                "teachmode": "200002",
//                "users": "初级人员",
//                "tags": "13213213",
//                "grade": "204001",
//                "description": "Java网络编程"
////  "objectives": "Java网络编程"
    }
}
