package com.cyan.springcloud.model.dto;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 课程预览数据模型
 *
 * @author Cyan Chau
 * @create 2023-07-04
 */
@Data
@ToString
public class CoursePreviewDto {

    // 课程基本信息 课程营销信息
    private CourseBaseInfoDto courseBase;

    // 课程计划信息
    private List<TeachplanDto> teachplans;


}
