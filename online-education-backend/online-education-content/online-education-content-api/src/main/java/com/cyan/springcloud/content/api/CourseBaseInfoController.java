package com.cyan.springcloud.content.api;

import com.cyan.springcloud.base.model.PageParams;
import com.cyan.springcloud.base.model.PageResult;
import com.cyan.springcloud.model.dto.AddCourseDto;
import com.cyan.springcloud.model.dto.CourseBaseInfoDto;
import com.cyan.springcloud.model.dto.QueryCourseParamsDto;
import com.cyan.springcloud.model.po.CourseBase;
import com.cyan.springcloud.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Cyan Chau
 * @create 2023-01-23
 */
@RestController
//@RequestMapping("/course")
@Api(value = "课程信息编辑接口", tags = "课程信息编辑接口")
public class CourseBaseInfoController {

    @Resource
    private CourseBaseInfoService courseBaseService;

    /**
     * 课程查询接口
     *
     * @param pageParams 分页参数
     * @param queryCourseParamsDto 请求参数
     * @return
     */
    @ApiOperation("课程查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParamsDto) {
        PageResult<CourseBase> result = courseBaseService.queryCourseBaseList(pageParams, queryCourseParamsDto);

        return result;
    }


    @ApiOperation("新增课程基础信息")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody AddCourseDto addCourseDto){

        // 当前用户所属培训机构的id
        Long companyId = 22L;

        // service调用
        CourseBaseInfoDto courseBase = courseBaseService.createCourseBase(companyId, addCourseDto);

        return courseBase;
    }
}
