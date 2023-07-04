package com.cyan.springcloud.content.service;

import com.cyan.springcloud.model.dto.CoursePreviewDto;

/**
 * 课程预览逻辑接口
 *
 * @author Cyan Chau
 * @create 2023-07-04
 */
public interface CoursePublishService {

    /**
     * 获取课程预览信息
     *
     * @param courseId 课程id
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);

}
