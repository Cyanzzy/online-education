package com.cyan.springcloud.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * 请求模型类 课程查询参数Dto
 *
 * @author Cyan Chau
 * @create 2023-01-23
 */
@Data
@ToString
public class QueryCourseParamsDto {

    // 审核状态
    private String auditStatus;
    // 课程名称
    private String courseName;
    // 发布状态
    private String publishStatus;
}
