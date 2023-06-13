package com.cyan.springcloud.content.api;

import com.cyan.springcloud.content.service.CourseTeacherService;
import com.cyan.springcloud.model.dto.CourseCategoryTreeDto;
import com.cyan.springcloud.model.dto.CourseTeacherDto;
import com.cyan.springcloud.model.po.CourseTeacher;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 师资管理控制层
 *
 * @author Cyan Chau
 * @create 2023-06-13
 */
@RestController
@Api(value = "师资管理接口", tags = "师资管理接口")
public class CourseTeacherController {

    @Resource
    private CourseTeacherService courseTeacherService;

    @ApiOperation("查询教师接口")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> getCourseTeacherList(@PathVariable Long courseId) {
        return courseTeacherService.getCourseTeacherList(courseId);
    }

    @ApiOperation("添加/修改教师接口")
    @PostMapping("/courseTeacher")
    public CourseTeacher saveCourseTeacher(@RequestBody CourseTeacherDto courseTeacherDto) {
        return courseTeacherService.saveCourseTeacher(courseTeacherDto);
    }

    @ApiOperation("添加/修改教师接口")
    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    public void deleteCourseTeacher(@PathVariable Long courseId, @PathVariable Long teacherId) {
        courseTeacherService.deleteCourseTeacher(courseId, teacherId);
    }
}


