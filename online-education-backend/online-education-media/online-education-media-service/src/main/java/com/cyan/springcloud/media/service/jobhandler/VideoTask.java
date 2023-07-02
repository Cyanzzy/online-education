package com.cyan.springcloud.media.service.jobhandler;

import com.cyan.springcloud.base.utils.Mp4VideoUtil;
import com.cyan.springcloud.media.model.po.MediaProcess;
import com.cyan.springcloud.media.service.MediaFileProcessService;
import com.cyan.springcloud.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * 视频处理类
 *
 * @author Cyan Chau
 * @create 2023-07-02
 */
@Slf4j
@Component
public class VideoTask {

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegpath;

    @Resource
    private MediaFileService mediaFileService;

    @Resource
    private MediaFileProcessService mediaFileProcessService;

    /**
     * 分片广播：视频处理任务
     */
    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex(); // 执行器序号，从0开始
        int shardTotal = XxlJobHelper.getShardTotal(); // 执行器总数

        // 确定CPU核心数
        int processors = Runtime.getRuntime().availableProcessors();

        // 查询待处理的任务
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
        // 任务数量
        int size = mediaProcessList.size();
        log.info(">>>>>>>>>>>>>取到的视频任务数：" + size);
        if (mediaProcessList == null || mediaProcessList.size() <= 0) {
            return;
        }
        // 创建线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(size);

        // 创建计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);

        mediaProcessList.forEach(mediaProcess -> {
            // 将任务加入线程池
            threadPool.execute(() -> {
                try {
                    // 任务id
                    Long taskId = mediaProcess.getId();
                    // 文件id，即MD5
                    String fileId = mediaProcess.getFileId();
                    // 桶
                    String bucket = mediaProcess.getBucket();
                    // 对象名
                    String objectName = mediaProcess.getFilePath();

                    // ***********开启任务
                    Boolean isStartTaskSuccess = mediaFileProcessService.startTask(taskId);
                    if (!isStartTaskSuccess) {
                        log.info(">>>>>>>>>>>任务抢占失败，任务id：{}", taskId);
                        return;
                    }

                    // **********从minio下载视频到本地
                    File file = mediaFileService.downloadFileFromMinIOOld(bucket, objectName);
                    if (file == null) {
                        log.error(">>>>>>>>>下载视频出错，任务id：{}，bucket：{}，objectName：{}", taskId, bucket, objectName);
                        // 保存任务失败的结果
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载视频到本地失败");
                        return;
                    }

                    // 源avi视频的路径
                    String videoPath = file.getAbsolutePath();
                    // 转换成MP4文件名
                    String mp4Name = fileId + ".mp4";

                    // ********** 转换后的mp4文件路径
                    // 创建临时文件，作为转换后的文件
                    File mp4File = null;
                    try {
                        mp4File = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.error(">>>>>>>>>>>>>>>>>创建临时文件异常, {}", e.getMessage());
                        // 保存任务失败的结果
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件异常");
                        return;
                    }
                    String mp4Path = mp4File.getAbsolutePath();

                    // 创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, videoPath, mp4Name, mp4Path);
                    // *************开始视频转换
                    String result = videoUtil.generateMp4();
                    if (!result.equals("success")) {
                        log.error("视频转换失败，原因：{}，bucket：{}，objectName：{}", result, bucket, objectName);
                        // 保存任务失败的结果
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "视频转码失败");
                        return;
                    }
                    log.info(">>>>>>>>>>>>>>>>视频转换成功，准备上传到minio");
                    String mimeType = "video/mp4";
                    // ********************上传到minio
                    //上传到minio的路径
                    String uploadObjectName = getFilePath(fileId, ".mp4");
                    boolean isAddSucess = mediaFileService.addMediaFilesToMinIO(mp4File.getAbsolutePath(), mimeType, bucket, uploadObjectName);
                    if (!isAddSucess) {
                        log.error("上传mp4到MinIO失败，taskId：{}", taskId);
                        // 保存任务失败的结果
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "上传mp4到MinIO失败");
                        return;
                    }

                    // mp4文件的url
                    String url = getFilePath(fileId, ".mp4");

                    // 任务状态-->成功
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, "保存文件成功");
                } finally {
                    // 更新计数器
                    countDownLatch.countDown();
                }


            });
        });

        // 阻塞线程，正常情况下，线程跑完解除阻塞；非正常情况下，超过三十分钟自动解除阻塞
        countDownLatch.await(30, TimeUnit.MINUTES);

    }

    /**
     * 拼接文件路径
     *
     * @param fileMd5 文件md5值
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePath(String fileMd5, String fileExt){
        return fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }


}
