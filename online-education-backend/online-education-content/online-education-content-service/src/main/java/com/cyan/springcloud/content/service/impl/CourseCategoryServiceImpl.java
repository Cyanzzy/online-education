package com.cyan.springcloud.content.service.impl;

import com.cyan.springcloud.content.mapper.CourseCategoryMapper;
import com.cyan.springcloud.content.service.CourseCategoryService;
import com.cyan.springcloud.model.dto.CourseCategoryTreeDto;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 课程分类
 *
 * @author Cyan Chau
 * @create 2023-02-10
 */
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Resource
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        return courseCategoryMapper.selectTreeNodes();
    }
}
