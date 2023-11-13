package com.cyan.springcloud.model.dto;

import com.cyan.springcloud.model.po.Teachplan;
import com.cyan.springcloud.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 请求模型类 课程计划信息Dto
 *
 * @author Cyan Chau
 * @create 2023-06-11
 */
@Data
@ToString
public class TeachplanDto extends Teachplan {

    /**
     * 媒资相关信息
     */
    private TeachplanMedia teachplanMedia;

    /**
     * 小章节
     */
    private List<TeachplanDto> teachPlanTreeNodes;

}
