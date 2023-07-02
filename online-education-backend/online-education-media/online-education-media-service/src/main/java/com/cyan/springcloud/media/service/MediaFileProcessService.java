package com.cyan.springcloud.media.service;

import com.cyan.springcloud.media.model.po.MediaProcess;

import java.util.List;

/**
 * 任务处理
 *
 * @author Cyan Chau
 * @create 2023-07-02
 */
public interface MediaFileProcessService {

    /**
     * 获取待处理任务
     *
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count 获取记录数
     */
    List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);

    /**
     * 开启任务
     * @param id 任务id
     * @return
     */
    Boolean startTask(Long id);

    /**
     * 保存任务结果
     *
     * @param taskId  任务id
     * @param status 任务状态
     * @param fileId  文件id
     * @param url url
     */
    void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg);


}
