package com.cyan.springcloud.search.controller;

import com.cyan.springcloud.base.model.PageParams;
import com.cyan.springcloud.search.dto.SearchCourseParamDto;
import com.cyan.springcloud.search.dto.SearchPageResultDto;
import com.cyan.springcloud.search.po.CourseIndex;
import com.cyan.springcloud.search.service.CourseSearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 课程搜索接口
 */
@RestController
@RequestMapping("/course")
@Api(value = "课程搜索接口", tags = "课程搜索接口")
public class CourseSearchController {

    @Resource
    private CourseSearchService courseSearchService;

    @ApiOperation("课程搜索列表")
    @GetMapping("/list")
    public SearchPageResultDto<CourseIndex> list(PageParams pageParams, SearchCourseParamDto searchCourseParamDto) {

        return courseSearchService.queryCoursePubIndex(pageParams, searchCourseParamDto);

    }
}
