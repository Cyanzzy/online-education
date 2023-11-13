package com.cyan.springcloud.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.springcloud.base.exception.BusinessException;
import com.cyan.springcloud.base.model.PageResult;
import com.cyan.springcloud.base.model.RestResponse;
import com.cyan.springcloud.learning.feignclient.ContentServiceClient;
import com.cyan.springcloud.learning.feignclient.MediaServiceClient;
import com.cyan.springcloud.learning.mapper.XcCourseTablesMapper;
import com.cyan.springcloud.learning.model.dto.MyCourseTableParams;
import com.cyan.springcloud.learning.model.dto.XcCourseTablesDto;
import com.cyan.springcloud.learning.model.po.XcCourseTables;
import com.cyan.springcloud.learning.service.LearningService;
import com.cyan.springcloud.learning.service.MyCourseTablesService;
import com.cyan.springcloud.model.po.CoursePublish;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Cyan Chau
 * @create 2023-07-08
 */
@Service
public class LearningServiceImpl implements LearningService {

    @Resource
    private ContentServiceClient contentServiceClient;

    @Resource
    private MyCourseTablesService myCourseTablesService;

    @Resource
    private MediaServiceClient mediaServiceClient;

    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        //查询课程信息
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (coursepublish == null) {
            BusinessException.cast("课程信息不存在");
        }
        //校验学习资格

        //如果登录
        if (StringUtils.isNotEmpty(userId)) {

            //判断是否选课，根据选课情况判断学习资格
            XcCourseTablesDto xcCourseTablesDto = myCourseTablesService.getLearningStatus(userId, courseId);
            //学习资格状态 [{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
            String learnStatus = xcCourseTablesDto.getLearnStatus();
            if (learnStatus.equals("702001")) {
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            } else if (learnStatus.equals("702003")) {
                RestResponse.validfail("您的选课已过期需要申请续期或重新支付");
            }
        }

        //未登录或未选课判断是否收费
        String charge = coursepublish.getCharge();
        if (charge.equals("201000")) {//免费可以正常学习
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }

        return RestResponse.validfail("请购买课程后继续学习");
    }


}
