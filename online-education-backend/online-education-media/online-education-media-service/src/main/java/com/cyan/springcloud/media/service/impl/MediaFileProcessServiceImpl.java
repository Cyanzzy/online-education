package com.cyan.springcloud.media.service.impl;

import com.cyan.springcloud.media.mapper.MediaFilesMapper;
import com.cyan.springcloud.media.mapper.MediaProcessHistoryMapper;
import com.cyan.springcloud.media.mapper.MediaProcessMapper;
import com.cyan.springcloud.media.model.po.MediaFiles;
import com.cyan.springcloud.media.model.po.MediaProcess;
import com.cyan.springcloud.media.model.po.MediaProcessHistory;
import com.cyan.springcloud.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务处理逻辑实现
 *
 * @author Cyan Chau
 * @create 2023-07-02
 */
@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    @Resource
    private MediaFilesMapper mediaFilesMapper;

    @Resource
    private MediaProcessMapper mediaProcessMapper;

    @Resource
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
    }

    @Override
    public Boolean startTask(Long id) {
        int result = mediaProcessMapper.startTask(id);

        return result > 0;
    }

    @Override
    @Transactional
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        // 待更新的任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);

        if (mediaProcess == null) {
            return;
        }
        // 如果任务执行失败
        if (status.equals("3")) {
            // 更新media_process的状态
            mediaProcess.setStatus("3"); // 任务失败
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
            mediaProcess.setErrormsg(errorMsg);

            mediaProcessMapper.updateById(mediaProcess);
            return;
        }
        // ********如果任务执行成功
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId); // 文件表记录
        // 更新media_file中的url
        mediaFiles.setUrl(url);
        mediaFilesMapper.updateById(mediaFiles);

        // 更新media_process的状态
        mediaProcess.setStatus("2");
        // 更新media_process的完成时间
        mediaProcess.setFinishDate(LocalDateTime.now());
        // 更新media_process的Url
        mediaProcess.setUrl(url);
        mediaProcessMapper.updateById(mediaProcess);

        // 将media_process表的记录插入到media_process_history表
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);

        // 从media_process删除当前任务
        mediaProcessMapper.deleteById(taskId);

    }
}
