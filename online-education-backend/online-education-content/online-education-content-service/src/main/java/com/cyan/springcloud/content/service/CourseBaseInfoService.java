package com.cyan.springcloud.content.service;

import com.cyan.springcloud.base.model.PageParams;
import com.cyan.springcloud.base.model.PageResult;
import com.cyan.springcloud.model.dto.AddCourseDto;
import com.cyan.springcloud.model.dto.CourseBaseInfoDto;
import com.cyan.springcloud.model.dto.EditCourseDto;
import com.cyan.springcloud.model.dto.QueryCourseParamsDto;
import com.cyan.springcloud.model.po.CourseBase;

/**
 * 课程管理
 *
 * @author Cyan Chau
 * @create 2023-01-25
 */
public interface CourseBaseInfoService {

    /**
     * 课程查询
     *
     * @param companyId 教学机构id
     * @param pageParams 分页参数
     * @param queryCourseParamsDto 查询条件
     * @return
     */
    PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /**
     * 添加课程基本信息
     *
     * @param companyId 教学机构id
     * @param addCourseDto 课程基本信息
     * @return 课程信息：基本信息、营销信息
     */
    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    /**
     * 根据课程Id查询课程信息
     *
     * @param courseId 课程id
     * @return
     */
    CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    /**
     * 修改课程
     *
     * @param companyId 机构Id
     * @param editCourseDto 修改课程信息
     * @return 课程详细信息
     */
    CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto);

    /**
     * 删除课程
     *
     * @param companyId 机构id
     * @param courseId 课程id
     */
    void deleteCourse(Long companyId, Long courseId);
}
