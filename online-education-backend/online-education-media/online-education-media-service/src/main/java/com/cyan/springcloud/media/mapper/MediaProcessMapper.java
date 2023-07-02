package com.cyan.springcloud.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyan.springcloud.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    /**
     * 根据分片参数获取待处理任务
     *
     * @param shardTotal 分片总数
     * @param shardIndex 分片序号
     * @param count 任务数
     * @return
     */
    List<MediaProcess> selectListByShardIndex(@Param("shardTotal") int shardTotal, @Param("shardIndex") int shardIndex, @Param("count") int count);

    /**
     * 开启一个任务
     *
     * @param id 任务id
     * @return 更新记录数
     */
    int startTask(@Param("id") long id);

}
