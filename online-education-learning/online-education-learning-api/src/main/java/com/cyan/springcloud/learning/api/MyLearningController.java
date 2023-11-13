package com.cyan.springcloud.learning.api;

import com.cyan.springcloud.base.model.RestResponse;
import com.cyan.springcloud.learning.service.LearningService;
import com.cyan.springcloud.learning.util.SecurityUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 *  我的学习接口
 */
@RestController
@Api(value = "学习过程管理接口", tags = "学习过程管理接口")
public class MyLearningController {

    @Resource
    private LearningService learningService;

    @ApiOperation("获取视频")
    @GetMapping("/open/learn/getvideo/{courseId}/{teachplanId}/{mediaId}")
    public RestResponse<String> getvideo(@PathVariable("courseId") Long courseId, @PathVariable("courseId") Long teachplanId, @PathVariable("mediaId") String mediaId) {
        // 登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String userId = null;
        if(user != null){
            userId = user.getId();
        }

        return learningService.getVideo(userId, courseId, teachplanId, mediaId);

    }

}
