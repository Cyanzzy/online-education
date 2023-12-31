package com.cyan.springcloud.content.api;

import com.cyan.springcloud.content.service.TeachplanService;
import com.cyan.springcloud.model.dto.BindTeachplanMediaDto;
import com.cyan.springcloud.model.dto.SaveTeachplanDto;
import com.cyan.springcloud.model.dto.TeachplanDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

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
    private TeachplanService teachplanService;

    @ApiOperation("查询课程计划树形接口")
    @ApiImplicitParam(value = "courseId",name = "课程Id",required = true,dataType = "Long",paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId) {
        return teachplanService.selectTreeNodes(courseId);
    }

    @ApiOperation("课程计划创建或修改接口")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto teachplan){
        teachplanService.saveTeachplan(teachplan);
    }


    @ApiOperation("课程计划删除接口")
    @DeleteMapping("/teachplan/{teachPlanId}")
    public void deleteTeachplan(@PathVariable Long teachPlanId){
        teachplanService.deleteTeachplan(teachPlanId);
    }

    @ApiOperation("课程计划排序接口")
    @PostMapping("/teachplan/{moveType}/{teachPlanId}")
    public void orderByTeachplan(@PathVariable String moveType, @PathVariable Long teachPlanId){
        teachplanService.orderByTeachplan(moveType, teachPlanId);
    }


    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto) {
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

    @ApiOperation("课程计划解除媒资信息绑定")
    @DeleteMapping("/teachplan/association/media/{teachPlanId}/{mediaId}")
    public void unassociationMedia(@PathVariable Long teachPlanId, @PathVariable Long mediaId) {
        teachplanService.unassociationMedia(teachPlanId, mediaId);
    }

}
