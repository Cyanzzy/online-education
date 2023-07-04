package com.cyan.springcloud.content.api;

import com.cyan.springcloud.content.service.CourseBaseInfoService;
import com.cyan.springcloud.content.service.CoursePublishService;
import com.cyan.springcloud.model.dto.CoursePreviewDto;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 课程公开查询接口
 *
 * @author Cyan Chau
 * @create 2023-07-04
 */
@Api(value = "课程公开查询接口", tags = "课程公开查询接口")
@RestController
@RequestMapping("/open")
public class CourseOpenController {

    @Resource
    private CourseBaseInfoService courseBaseInfoService;

    @Resource
    private CoursePublishService coursePublishService;


    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getPreviewInfo(@PathVariable("courseId") Long courseId) {

        // 获取课程预览信息
        return coursePublishService.getCoursePreviewInfo(courseId);
    }

}
