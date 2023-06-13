package com.cyan.springcloud.content.api;

import com.cyan.springcloud.content.service.TeacplanService;
import com.cyan.springcloud.model.dto.SaveTeachplanDto;
import com.cyan.springcloud.model.dto.TeachplanDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * 课程计划管理控制层
 *
 * @author Cyan Chau
 * @create 2023-06-11
 */
@RestController
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
public class TeacheplanController {

    @Resource
    private TeacplanService teacplanService;

    @ApiOperation("查询课程计划树形接口")
    @ApiImplicitParam(value = "courseId",name = "课程Id",required = true,dataType = "Long",paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId) {
        return teacplanService.selectTreeNodes(courseId);
    }

    @ApiOperation("课程计划创建或修改接口")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto teachplan){
        teacplanService.saveTeachplan(teachplan);
    }


    @ApiOperation("课程计划删除接口")
    @DeleteMapping("/teachplan/{teachPlanId}")
    public void deleteTeachplan(@PathVariable Long teachPlanId){
        teacplanService.deleteTeachplan(teachPlanId);
    }

    @ApiOperation("课程计划排序接口")
    @PostMapping("/teachplan/{moveType}/{teachPlanId}")
    public void orderByTeachplan(@PathVariable String moveType, @PathVariable Long teachPlanId){
        teacplanService.orderByTeachplan(moveType, teachPlanId);
    }
}
