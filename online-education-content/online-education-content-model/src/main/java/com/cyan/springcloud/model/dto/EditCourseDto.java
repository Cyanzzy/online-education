package com.cyan.springcloud.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 请求模型类 修改课程dto
 *
 * @author Cyan Chau
 * @create 2023-06-09
 */
@Data
public class EditCourseDto extends AddCourseDto{

    @ApiModelProperty(value = "课程Id", required = true)
    private Long id;
}
