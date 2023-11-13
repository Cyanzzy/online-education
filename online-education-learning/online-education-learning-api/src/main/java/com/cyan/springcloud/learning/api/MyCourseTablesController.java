package com.cyan.springcloud.learning.api;

import com.cyan.springcloud.base.exception.BusinessException;
import com.cyan.springcloud.base.model.PageResult;
import com.cyan.springcloud.learning.model.dto.MyCourseTableParams;
import com.cyan.springcloud.learning.model.dto.XcChooseCourseDto;
import com.cyan.springcloud.learning.model.dto.XcCourseTablesDto;
import com.cyan.springcloud.learning.model.po.XcCourseTables;

import com.cyan.springcloud.learning.service.MyCourseTablesService;
import com.cyan.springcloud.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 我的课程表接口
 */

@Api(value = "我的课程表接口", tags = "我的课程表接口")
@Slf4j
@RestController
public class MyCourseTablesController {

    @Resource
    private MyCourseTablesService myCourseTablesService;

    @ApiOperation("添加选课")
    @PostMapping("/choosecourse/{courseId}")
    public XcChooseCourseDto addChooseCourse(@PathVariable("courseId") Long courseId) {

        // 当前登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            BusinessException.cast("请先登录");
        }
        // 用户id
        String userId = user.getId();
        // 添加选课
        return myCourseTablesService.addChooseCourse(userId, courseId);

    }

    @ApiOperation("查询学习资格")
    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public XcCourseTablesDto getLearnstatus(@PathVariable("courseId") Long courseId) {

        // 当前登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            BusinessException.cast("请先登录");
        }
        // 用户id
        String userId = user.getId();
        // 查询学习资格
        return myCourseTablesService.getLearningStatus(userId, courseId);

    }

    @ApiOperation("我的课程表")
    @GetMapping("/mycoursetable")
    public PageResult<XcCourseTables> mycoursetable(MyCourseTableParams params) {
        // 登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            BusinessException.cast("请登录后继续选课");
        }
        String userId = user.getId();
        // 设置当前的登录用户
        params.setUserId(userId);

        return myCourseTablesService.mycourestabls(params);
    }

}
