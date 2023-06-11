package com.cyan.springcloud.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyan.springcloud.model.dto.TeachplanDto;
import com.cyan.springcloud.model.po.Teachplan;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author CyanChau
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    /**
     * 课程计划查询
     *
     * @param courseId 课程id
     * @return
     */
    List<TeachplanDto> selectTreeNodes(Long courseId);
}
