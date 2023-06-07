package com.cyan.springcloud.content.service;

import com.cyan.springcloud.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * 课程分类逻辑接口
 *
 * @author Cyan Chau
 * @create 2023-02-10
 */
public interface CourseCategoryService {

    /**
     * 课程分类树形结构查询
     *
     * @param id
     * @return
     */
    List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
