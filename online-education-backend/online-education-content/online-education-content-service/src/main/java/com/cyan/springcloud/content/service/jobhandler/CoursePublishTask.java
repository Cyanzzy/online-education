package com.cyan.springcloud.content.service.jobhandler;

import com.cyan.springcloud.base.exception.BusinessException;

import com.cyan.springcloud.content.feignclient.CourseIndex;
import com.cyan.springcloud.content.feignclient.SearchServiceClient;
import com.cyan.springcloud.content.mapper.CoursePublishMapper;
import com.cyan.springcloud.content.service.CoursePublishService;
import com.cyan.springcloud.messagesdk.model.po.MqMessage;
import com.cyan.springcloud.messagesdk.service.MessageProcessAbstract;
import com.cyan.springcloud.messagesdk.service.MqMessageService;
import com.cyan.springcloud.model.po.CoursePublish;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * 课程发布任务类
 *
 * @author Cyan Chau
 * @create 2023-07-05
 */
@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Resource
    private CoursePublishService coursePublishService;

    @Resource
    private SearchServiceClient searchServiceClient;

    @Resource
    private CoursePublishMapper coursePublishMapper;

    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex(); // 执行器序号
        int shardTotal = XxlJobHelper.getShardTotal(); // 执行器总数

        // 调用抽象类的方法执行任务
        String messageType = "course_publish";
        int count = 30;
        long timeout = 60;

        log.debug("shardIndex=" + shardIndex + ",shardTotal=" + shardTotal);
        process(shardIndex, shardTotal, messageType, count, timeout);
    }


    /**
     * 发布任务逻辑
     *
     * @param mqMessage 执行任务内容
     * @return
     */
    @Override
    public boolean execute(MqMessage mqMessage) {

        // 从mqMessage拿到课程id
        Long courseId = Long.valueOf(mqMessage.getBusinessKey1());

        // 课程信息页面静态化上传到minio
        generateCourseStaticHtml(mqMessage, courseId);

        // 向elasticsearch写索引数据
        saveCourseIndex(mqMessage, courseId);

        // 向redis写缓存
        // TODO

        return true;
    }


    /**
     * 课程信息页面静态化上传到minio
     *
     * @param mqMessage 消息
     * @param courseId  课程id
     */
    private void generateCourseStaticHtml(MqMessage mqMessage, Long courseId) {

        // 消息id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();

        // 任务幂等性处理
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne > 0) {
            log.info("课程静态化任务完成，无需处理....");
            return;
        }

        // 开始进行课程静态化
        File file = coursePublishService.generateCourseHtml(courseId);
        if (file == null) {
            BusinessException.cast("生成的静态文件为空");
        }
        // 将生成的静态文件上传分布式文件系统
        coursePublishService.uploadCourseHtml(courseId, file);

        // 任务处理完成， 任务状态-->完成
        mqMessageService.completedStageOne(taskId);
    }

    /**
     * 向elasticsearch写索引数据
     *
     * @param mqMessage
     * @param courseId
     */
    private void saveCourseIndex(MqMessage mqMessage, Long courseId) {

        // 消息id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();

        // 任务幂等性处理
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if (stageTwo > 0) {
            log.info("课程索引信息写入任务完成，无需处理....");
            return;
        }

        // 查询课程信息，调用搜索服务添加索引接口

        // 从课程发布表查询课程信息
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish, courseIndex);
        // 远程调用搜索服务
        Boolean add = searchServiceClient.add(courseIndex);
        if (!add) {
           BusinessException.cast("远程调用添加索引服务失败");
        }

        // 任务处理完成， 任务状态-->完成
        mqMessageService.completedStageOne(taskId);
    }

}
