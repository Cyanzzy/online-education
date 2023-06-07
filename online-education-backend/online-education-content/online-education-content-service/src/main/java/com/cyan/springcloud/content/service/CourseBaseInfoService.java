package com.cyan.springcloud.content.service;

import com.cyan.springcloud.base.model.PageParams;
import com.cyan.springcloud.base.model.PageResult;
import com.cyan.springcloud.model.dto.AddCourseDto;
import com.cyan.springcloud.model.dto.CourseBaseInfoDto;
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
     * @param pageParams 分页参数
     * @param queryCourseParamsDto 查询条件
     * @return
     */
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /**
     * 添加课程基本信息
     *
     * @param companyId 教学机构id
     * @param addCourseDto 课程基本信息
     * @return 课程信息：基本信息、营销信息
     */
    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);
}
