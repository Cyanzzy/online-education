package com.cyan.springcloud.learning.service;

import com.cyan.springcloud.base.model.PageResult;
import com.cyan.springcloud.learning.model.dto.MyCourseTableParams;
import com.cyan.springcloud.learning.model.dto.OlChooseCourseDto;
import com.cyan.springcloud.learning.model.dto.OlCourseTablesDto;
import com.cyan.springcloud.learning.model.po.OlCourseTables;

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
    OlChooseCourseDto addChooseCourse(String userId, Long courseId);

    /**
     * 判断学习资格
     *
     * @param userId
     * @param courseId
     */
    OlCourseTablesDto getLearningStatus(String userId, Long courseId);


    /**
     * 保存选课记录
     *
     * @param chooseCourseId
     * @return
     */
    boolean saveChooseCourseStatus(String chooseCourseId);

    /**
     * 我的课程表
     *
     * @param params
     */
    PageResult<OlCourseTables> mycourestabls(MyCourseTableParams params);

}
