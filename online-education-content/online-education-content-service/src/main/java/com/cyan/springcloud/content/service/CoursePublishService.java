package com.cyan.springcloud.content.service;

import com.cyan.springcloud.model.dto.CoursePreviewDto;
import com.cyan.springcloud.model.po.CoursePublish;

import java.io.File;

/**
 * 课程预览逻辑接口
 *
 * @author Cyan Chau
 * @create 2023-07-04
 */
public interface CoursePublishService {

    /**
     * 获取课程预览信息
     *
     * @param courseId 课程id
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 提交审核
     *
     * @param courseId  课程id
     */
    void commitAudit(Long companyId,Long courseId);

    /**
     * 课程发布接口
     *
     * @param companyId 机构id
     * @param courseId 课程id
     */
    void publish(Long companyId,Long courseId);

    /**
     * 课程静态化
     *
     * @param courseId  课程id
     * @return File 静态化文件
     */
    File generateCourseHtml(Long courseId);

    /**
     * 上传课程静态化页面
     *
     * @param file  静态化文件
     */
    void  uploadCourseHtml(Long courseId,File file);

    /**
     * 根据课程id查询课程发布消息
     *
     * @param courseId
     * @return
     */
    CoursePublish getCoursePublish(Long courseId);
}
