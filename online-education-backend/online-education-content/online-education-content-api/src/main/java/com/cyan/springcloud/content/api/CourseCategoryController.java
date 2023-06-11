package com.cyan.springcloud.content.api;

import com.cyan.springcloud.content.service.CourseCategoryService;
import com.cyan.springcloud.model.dto.CourseCategoryTreeDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 课程分类信息控制层
 *
 * @author Cyan Chau
 * @create 2023-02-04
 */
@Slf4j
@RestController
@RequestMapping("/course-category")
@Api(value = "课程分类接口", tags = "课程分类接口")
public class CourseCategoryController {

    @Resource
    private CourseCategoryService courseCategoryService;

    @ApiOperation("课程分类查询接口")
    @GetMapping("/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        return courseCategoryService.queryTreeNodes("1");
    }
}
