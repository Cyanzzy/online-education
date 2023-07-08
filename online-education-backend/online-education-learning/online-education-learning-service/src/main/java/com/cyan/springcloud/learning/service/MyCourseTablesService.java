package com.cyan.springcloud.learning.service;

import com.cyan.springcloud.learning.model.dto.XcChooseCourseDto;
import com.cyan.springcloud.learning.model.dto.XcCourseTablesDto;

/**
 * 我的课程表业务逻辑接口
 *
 * @author Cyan Chau
 * @create 2023-07-08
 */
public interface MyCourseTablesService {

    /**
     * 添加选课
     *
     * @param userId   用户id
     * @param courseId 课程id
     */
    XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    /**
     * 判断学习资格
     *
     * @param userId
     * @param courseId
     */
    XcCourseTablesDto getLearningStatus(String userId, Long courseId);

}
