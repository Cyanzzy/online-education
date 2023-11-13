package com.cyan.springcloud.content.api;

import com.cyan.springcloud.base.exception.ValidationGroups;
import com.cyan.springcloud.base.model.PageParams;
import com.cyan.springcloud.base.model.PageResult;
import com.cyan.springcloud.base.utils.StringUtil;
import com.cyan.springcloud.content.utils.SecurityUtil;
import com.cyan.springcloud.model.dto.AddCourseDto;
import com.cyan.springcloud.model.dto.CourseBaseInfoDto;
import com.cyan.springcloud.model.dto.EditCourseDto;
import com.cyan.springcloud.model.dto.QueryCourseParamsDto;
import com.cyan.springcloud.model.po.CourseBase;
import com.cyan.springcloud.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.security.acl.LastOwnerException;

/**
 * 课程基础信息控制层
 *
 * @author Cyan Chau
 * @create 2023-01-23
 */
@RestController
//@RequestMapping("/course")
@Api(value = "课程信息编辑接口", tags = "课程信息编辑接口")
public class CourseBaseInfoController {

    @Resource
    private CourseBaseInfoService courseBaseService;

    /**
     * 课程查询接口
     *
     * @param pageParams           分页参数
     * @param queryCourseParamsDto 请求参数
     * @return
     */
    @ApiOperation("课程查询接口")
    @PostMapping("/course/list")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParamsDto) {

        // 获取用户身份
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        // 获取机构id
        Long companyId = null;
        if (StringUtils.isNotEmpty(user.getCompanyId())) {
            companyId = Long.parseLong(user.getCompanyId());
        }

        return courseBaseService.queryCourseBaseList(companyId, pageParams, queryCourseParamsDto);
    }

    @ApiOperation("新增课程基础信息接口")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidationGroups.Insert.class) AddCourseDto addCourseDto) {

        // 当前用户所属培训机构的id
        Long companyId = 22L;

        // service调用
        return courseBaseService.createCourseBase(companyId, addCourseDto);
    }

    @ApiOperation("根据id查询课程信息接口")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId) {
//        // 获取当前用户身份
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        System.out.println(">>>>>>>>>>>>>>>>>>>principal=: " + principal);
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        System.out.println(">>>>>>>>>>>>>>>>>> username: " + user.getUsername());
        return courseBaseService.getCourseBaseInfo(courseId);
    }

    @ApiOperation("修改课程")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated(ValidationGroups.Update.class) EditCourseDto editCourseDto) {
        // 当前用户所属培训机构的id
        Long companyId = 1232141425L;
        return courseBaseService.updateCourseBase(companyId, editCourseDto);
    }

    @ApiOperation("删除课程")
    @DeleteMapping("/course/{courseId}")
    public void modifyCourseBase(@PathVariable Long courseId) {
        // 当前用户所属培训机构的id
        Long companyId = 1232141425L;
        courseBaseService.deleteCourse(companyId, courseId);
    }
}
