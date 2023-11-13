package com.cyan.springcloud.messagesdk;

import com.cyan.springcloud.messagesdk.model.po.MqMessage;
import com.cyan.springcloud.messagesdk.service.MessageProcessAbstract;
import com.cyan.springcloud.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * MessageProcessClass
 *
 * @author Cyan Chau
 * @create 2023-07-05
 */
@Slf4j
@Component
public class MessageProcessClass extends MessageProcessAbstract {

    @Resource
    private MqMessageService mqMessageService;

    @Override
    public boolean execute(MqMessage mqMessage) {
        Long id = mqMessage.getId();
        log.debug("开始执行任务:{}",id);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 取出阶段状态
        int stageOne = mqMessageService.getStageOne(id);
        if(stageOne<1){
            log.debug("开始执行第一阶段任务");
            System.out.println();
            int i = mqMessageService.completedStageOne(id);
            if(i>0){
                log.debug("完成第一阶段任务");
            }
        }else{
            log.debug("无需执行第一阶段任务");
        }
        return true;
    }
}
