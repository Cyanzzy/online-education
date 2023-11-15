package com.cyan.springcloud.learning.service;

import com.cyan.springcloud.base.model.RestResponse;

/**
 * 学习过程管理
 *
 * @author Cyan Chau
 * @create 2023-07-08
 */
public interface LearningService {

    /**
     * 获取教学视频
     *
     * @param courseId    课程id
     * @param teachplanId 课程计划id
     * @param mediaId     视频文件id
     */
    RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId);


}
