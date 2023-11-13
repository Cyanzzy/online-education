package com.cyan.springcloud.search.service;

import com.cyan.springcloud.base.model.PageParams;
import com.cyan.springcloud.search.dto.SearchCourseParamDto;
import com.cyan.springcloud.search.dto.SearchPageResultDto;
import com.cyan.springcloud.search.po.CourseIndex;

/**
 * 课程搜索service
 */
public interface CourseSearchService {


    /**
     * 搜索课程列表
     *
     * @param pageParams 分页参数
     * @param searchCourseParamDto 搜索条件
    */
    SearchPageResultDto<CourseIndex> queryCoursePubIndex(PageParams pageParams, SearchCourseParamDto searchCourseParamDto);

}
