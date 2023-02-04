package com.cyan.springcloud.model.dto;

import com.cyan.springcloud.model.po.CourseCategory;

import java.io.Serializable;
import java.util.List;

/**
 * 课程分类Dto
 *
 * @author Cyan Chau
 * @create 2023-02-04
 */
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {

    List childrenTreeNodes;
}
