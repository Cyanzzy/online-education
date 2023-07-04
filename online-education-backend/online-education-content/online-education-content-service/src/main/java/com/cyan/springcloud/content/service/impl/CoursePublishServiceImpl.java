package com.cyan.springcloud.content.service.impl;

import com.cyan.springcloud.content.service.CourseBaseInfoService;
import com.cyan.springcloud.content.service.CoursePublishService;
import com.cyan.springcloud.content.service.TeachplanService;
import com.cyan.springcloud.model.dto.CourseBaseInfoDto;
import com.cyan.springcloud.model.dto.CoursePreviewDto;
import com.cyan.springcloud.model.dto.TeachplanDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 课程预览逻辑接口实现
 *
 * @author Cyan Chau
 * @create 2023-07-04
 */
@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Resource
    private CourseBaseInfoService courseBaseInfoService;

    @Resource
    private TeachplanService teachplanService;


    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {

        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();

        // 课程基本信息 课程营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfo);

        // 课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        coursePreviewDto.setTeachplans(teachplanTree);

        return coursePreviewDto;
    }
}
