package com.cyan.springcloud.messagesdk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cyan.springcloud.messagesdk.model.po.MqMessage;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface MqMessageService extends IService<MqMessage> {

    /**
     * 扫描消息表记录，采用与扫描视频处理表相同的思路
     *
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count 扫描记录数
     */
    List<MqMessage> getMessageList(int shardIndex, int shardTotal, String messageType, int count);

    /**
     * 添加消息
     *
     * @param businessKey1 业务id
     * @param businessKey2 业务id
     * @param businessKey3 业务id
    */
    MqMessage addMessage(String messageType, String businessKey1, String businessKey2, String businessKey3);

    /**
     * 完成任务
     *
     * @param id 消息id
     * @return int 更新成功：1
     */
    int completed(long id);

    /**
     * 成阶段任务
     *
     * @param id 消息id
     * @return int 更新成功：1
     */
    int completedStageOne(long id);
    int completedStageTwo(long id);
    int completedStageThree(long id);
    int completedStageFour(long id);

    /**
     * 查询阶段状态
     *
     * @param id
    */
    int getStageOne(long id);
    int getStageTwo(long id);
    int getStageThree(long id);
    int getStageFour(long id);
}
