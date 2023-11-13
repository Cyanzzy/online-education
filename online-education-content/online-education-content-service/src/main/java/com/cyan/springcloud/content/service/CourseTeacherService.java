package com.cyan.springcloud.content.service;

import com.cyan.springcloud.model.dto.CourseTeacherDto;
import com.cyan.springcloud.model.po.CourseTeacher;

import java.util.List;

/**
 * 师资管理逻辑接口
 *
 * @author Cyan Chau
 * @create 2023-06-13
 */
public interface CourseTeacherService {

    /**
     * 获取师资信息
     *
     * @param courseId 课程id
     * @return
     */
    List<CourseTeacher> getCourseTeacherList(Long courseId);

    /**
     * 添加/修改师资信息
     *
     * @param courseTeacherDto
     * @return
     */
    CourseTeacher saveCourseTeacher(CourseTeacherDto courseTeacherDto);

    /**
     * 删除师资信息
     *
     * @param courseId 课程id
     * @param teacherId 教师id
     */
    void deleteCourseTeacher(Long courseId, Long teacherId);
}
