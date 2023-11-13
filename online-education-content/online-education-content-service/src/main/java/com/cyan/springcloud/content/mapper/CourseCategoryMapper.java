package com.cyan.springcloud.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyan.springcloud.model.dto.CourseCategoryTreeDto;
import com.cyan.springcloud.model.po.CourseCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author CyanChau
 */
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    List<CourseCategoryTreeDto> selectTreeNodes(String id);

}
