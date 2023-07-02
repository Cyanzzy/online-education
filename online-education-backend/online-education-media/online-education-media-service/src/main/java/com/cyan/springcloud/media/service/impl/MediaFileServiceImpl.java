package com.cyan.springcloud.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.springcloud.base.exception.BusinessException;
import com.cyan.springcloud.base.model.PageParams;
import com.cyan.springcloud.base.model.PageResult;
import com.cyan.springcloud.base.model.RestResponse;
import com.cyan.springcloud.media.mapper.MediaFilesMapper;
import com.cyan.springcloud.media.mapper.MediaProcessMapper;
import com.cyan.springcloud.media.model.dto.QueryMediaParamsDto;
import com.cyan.springcloud.media.model.dto.UploadFileParamsDto;
import com.cyan.springcloud.media.model.dto.UploadFileResultDto;
import com.cyan.springcloud.media.model.po.MediaFiles;
import com.cyan.springcloud.media.model.po.MediaProcess;
import com.cyan.springcloud.media.service.MediaFileService;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 媒资文件管理业务接口实现
 *
 * @author Cyan Chau
 * @create 2023-06-15
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Resource
    private MediaFilesMapper mediaFilesMapper;

    @Resource
    private MediaProcessMapper mediaProcessMapper;

    @Resource
    private MinioClient minioClient;

    @Resource
    private MediaFileService currentProxy;

    @Value("${minio.bucket.files}")
    private String bucketMediaFiles;

    @Value("${minio.bucket.videofiles}")
    private String bucketVideo;

    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        // 构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        // 在媒资列表可以查看到刚刚上传的图片信息，但是通过条件查询不起作用
        // 修复时间 2023-06-15
        queryWrapper.like(!StringUtils.isEmpty(queryMediaParamsDto.getFilename()), MediaFiles::getFilename, queryMediaParamsDto.getFilename());
        queryWrapper.eq(!StringUtils.isEmpty(queryMediaParamsDto.getFileType()), MediaFiles::getFileType, queryMediaParamsDto.getFileType());
        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        return new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    /**
     * 根据扩展名取到mimeType
     *
     * @param extension
     * @return
     */
    private String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }
        // 通过扩展名得到媒资类型mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    /**
     * 将文件上传到minio
     *
     * @param localFilePath 本地文件路径
     * @param mimeType      媒体类型
     * @param bucket        桶
     * @param objectName    对象
     * @return
     */
    public boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket) // 桶
                    .filename(localFilePath) // 本地文件路径
                    .object(objectName) // 对象名
                    .contentType(mimeType) // 媒体文件类型
                    .build();
            // 上传文件
            minioClient.uploadObject(uploadObjectArgs);
            log.debug("上传文件到minio成功, bucket：{}, objectName:{}", bucket, objectName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件出错, bucket:{}, objectName:{}, 错误信息：{}", bucket, objectName, e.getMessage());
        }
        return false;
    }

    /**
     * 获取文件默认存储目录路径 年/月/日
     *
     * @return
     */
    private String getDefaultFolderPath() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(new Date()).replace("-", "/") + "/";
    }

    /**
     * 获取文件的MD5
     *
     * @param file
     * @return
     */
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将文件信息保存到数据库
     *
     * @param companyId           机构id
     * @param fileMd5             文件md5值
     * @param uploadFileParamsDto 上传文件的信息
     * @param bucket              桶
     * @param objectName          对象
     * @return
     */
    @Override
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {

        // 将文件信息保存到数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);

        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            // 文件id
            mediaFiles.setId(fileMd5);
            // 机构id
            mediaFiles.setCompanyId(companyId);
            // 桶
            mediaFiles.setBucket(bucket);
            // file_path
            mediaFiles.setFilePath(objectName);
            // file_id
            mediaFiles.setFileId(fileMd5);
            // url
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            // 上传时间
            mediaFiles.setCreateDate(LocalDateTime.now());
            // 上传状态
            mediaFiles.setStatus("1");
            // 审核状态
            mediaFiles.setAuditStatus("002003");
            // 插入数据库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0) {
                log.debug("向数据库保存文件失败, bucket:{}, objectName:{}", bucket, objectName);
                return null;
            }
            // 记录待处理的任务
            addWaitingTask(mediaFiles);
            return mediaFiles;
        }

        return mediaFiles;
    }

    /**
     * 添加待处理任务
     *
     * @param mediaFiles
     */
    private void addWaitingTask(MediaFiles mediaFiles) {

        // 获取文件的mimeType
        // 文件名称
        String filename = mediaFiles.getFilename();
        // 文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);

        // 目前先测试.avi，如果有其他格式，使用配置文件导入配置即可
        if (mimeType.equals("video/x-msvideo")) {
            // 如果是.avi视频，则写入待处理任务
            MediaProcess mediaProcess = new MediaProcess();

            // 文件标识 文件名称 存储源
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            // 状态：未处理
            mediaProcess.setStatus("1");
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);
            mediaProcess.setUrl(null);

            mediaProcessMapper.insert(mediaProcess);
        }

    }

    @Override
//    @Transactional
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {

        // 文件名
        String filename = uploadFileParamsDto.getFilename();
        // 扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        // mimeType
        String mimeType = getMimeType(extension);
        // 子目录
        String defaultFolderPath = getDefaultFolderPath();
        // 文件md5
        String fileMd5 = getFileMd5(new File(localFilePath));
        String objectName = defaultFolderPath + fileMd5 + extension;
        // 上传文件到minio
        boolean result = addMediaFilesToMinIO(localFilePath, mimeType, bucketMediaFiles, objectName);

        if (!result) {
            BusinessException.cast("上传文件失败");
        }

        // 入库文件信息
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucketMediaFiles, objectName);

        if (mediaFiles == null) {
            BusinessException.cast("文件上传后保存信息失败");
        }

        // 返回对象
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);

        return uploadFileResultDto;
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        // 先查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            // 文件不存在
            return RestResponse.success(false);
        }

        // 如果数据库存在，再查询minio
        try {
            FilterInputStream inputStream = minioClient.getObject(GetObjectArgs
                    .builder()
                    .bucket(mediaFiles.getBucket())
                    .build());
            if (inputStream == null) {
                return RestResponse.success(false);
            }
        } catch (Exception e) {
            return RestResponse.success(false);
        }
        return RestResponse.success(true);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {

        // 获取分块目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        String chunkFilePath = chunkFileFolderPath + chunkIndex;

        // 检查分块是否存在
        try {
            FilterInputStream inputStream = minioClient.getObject(GetObjectArgs
                    .builder()
                    .bucket(bucketVideo)
                    .object(chunkFilePath)
                    .build());
            if (inputStream == null) {
                // 文件不存在
                return RestResponse.success(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return RestResponse.success(false);
        }
        // 文件存在
        return RestResponse.success(true);
    }

    /**
     * 得到分块文件的目录
     * 分块存储路径：md5前两位为两个目录，chunk存储分块文件
     *
     * @param fileMd5
     * @return
     */
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {

        // 分块文件的路径
        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunk;
        // mimeType
        String mimeType = getMimeType(null);
        // 将分块文件上传到minio
        boolean result = addMediaFilesToMinIO(localChunkFilePath, mimeType, bucketVideo, chunkFilePath);

        if (!result) {
            return RestResponse.validfail(false, "上传分块文件失败");
        }

        return RestResponse.success(true);
    }

    /**
     * 原始方法 有BUG
     *
     * @param companyId 机构id
     * @param fileMd5 文件md5
     * @param chunkTotal 文件总和
     * @param uploadFileParamsDto 文件信息
     * @return
     */
    @Override
    public RestResponse mergeChunksOld(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        // 找到分块文件进行合并

        // 分块文件所在目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);

        // 分块文件信息
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder().bucket(bucketVideo).object(chunkFileFolderPath + i).build())
                .collect(Collectors.toList());

        // 源文件名称
        String filename = uploadFileParamsDto.getFilename();

        // 扩展名
        String extension = filename.substring(filename.lastIndexOf("."));

        // 合并后文件的objectName
        String objectName = getFilePathByMd5(fileMd5, extension);

        // 参数信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucketVideo)
                .object(objectName) // 合并后的文件
                .sources(sources) // 指定源文件
                .build();

        // 合并文件 minio 分块默认5M
        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件出错, bucket:{},objectName:{},错误信息:{}", bucketVideo, objectName, e.getMessage());
            return RestResponse.validfail(false, "合并文件出错");
        }
        log.info(">>>>>>>>>>>>>>>>>>>合并文件成功");

        // 下载合并后的文件
        File file = downloadFileFromMinIOOld(bucketVideo, objectName);
        log.info(">>>>>>>>>>>>>>>>>>>下载合并后的文件成功");

        // 校验合并后的文件和源文件是否一致
        try (FileInputStream fileInputStream = new FileInputStream(file)){
            // 计算合并后的文件的md5
            String mergeFileMd5 = DigestUtils.md5Hex(fileInputStream);
            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>合并后的文件md5: " + mergeFileMd5 + "原始文件md5: " + fileMd5);

            // 比较源文件的md5值
            if (!fileMd5.equals(mergeFileMd5)) {
                log.error(">>>>>>>>>>>>>>>>>>>>>>>>>>校验合并文件的md5和原始文件md5不一致, 合并文件MD5:{}, 原始文件MD5:{}", mergeFileMd5, fileMd5);
                return RestResponse.validfail(false, "文件校验失败");
            }
            // 文件大小
            uploadFileParamsDto.setFileSize(file.length());
        } catch (Exception e) {
            return RestResponse.validfail(false, "文件校验失败");
        }

        log.info(">>>>>>>>>>>>>>>>>>校验合并后的文件和源文件成功，开始准备文件信息入库");

        // 将文件信息保存到数据库（非事务方法调用事务方法通过代理对象去调用，否则事务会失效）
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucketVideo, objectName);
        if (mediaFiles == null) {
            return RestResponse.validfail(false, "文件入库失败");
        }

        log.info(">>>>>>>>>>>>>>>>>>>>>>>文件信息入库成功，开始准备清理分块文件");

        // 清理分块文件
        clearChunkFiles(chunkFileFolderPath, chunkTotal);

        return RestResponse.success(true);
    }

    /**
     * 新方法
     *
     * @param companyId 机构id
     * @param fileMd5 文件md5
     * @param chunkTotal 文件总和
     * @param uploadFileParamsDto 文件信息
     * @return
     */
    @Override
    public RestResponse mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) throws IOException {
        // 下载分块文件
        File[] chunkFiles = checkChunkStatus(fileMd5, chunkTotal);
        // 获取源文件名
        String fileName = uploadFileParamsDto.getFilename();
        // 获取源文件扩展名
        String extension = fileName.substring(fileName.lastIndexOf("."));
        // 创建出临时文件，准备合并
        File mergeFile = null;
        try {
            mergeFile = File.createTempFile(fileName, extension);
        } catch (IOException e) {
            BusinessException.cast("创建合并临时文件出错");
        }
        try {
            // 缓冲区
            byte[] buffer = new byte[1024];
            // 写入流，向临时文件写入
            try (RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw")) {
                // 遍历分块文件数组
                for (File chunkFile : chunkFiles) {
                    // 读取流，读分块文件
                    try (RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r")) {
                        int len = -1;
                        while ((len = raf_read.read(buffer)) != -1) {
                            raf_write.write(buffer, 0, len);
                        }
                    }
                }
            } catch (Exception e) {
                BusinessException.cast("合并文件过程中出错");
            }
            uploadFileParamsDto.setFileSize(mergeFile.length());
            // 对文件进行校验，通过MD5值比较
            try (FileInputStream mergeInputStream = new FileInputStream(mergeFile)) {
                String mergeMd5 = DigestUtils.md5Hex(mergeInputStream);
                if (!fileMd5.equals(mergeMd5)) {
                    BusinessException.cast("合并文件校验失败");
                }
                log.debug("合并文件校验通过：{}", mergeFile.getAbsolutePath());
            } catch (Exception e) {
                BusinessException.cast("合并文件校验异常");
            }
            String mergeFilePath = getFilePathByMd5(fileMd5, extension);
            String mimeType = getMimeType(extension);
            // 合并后文件的objectName
            String objectName = getFilePathByMd5(fileMd5, extension);
            // 将本地合并好的文件，上传到minio中，这里重载了一个方法
            addMediaFilesToMinIO(mergeFile.getAbsolutePath(), mimeType, bucketVideo, mergeFilePath);
            log.debug("合并文件上传至MinIO完成{}", mergeFile.getAbsolutePath());
            // 将文件信息写入数据库
            MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucketVideo, objectName);
            if (mediaFiles == null) {
                BusinessException.cast("媒资文件入库出错");
            }
            log.debug("媒资文件入库完成");
            return RestResponse.success();
        } finally {
            for (File chunkFile : chunkFiles) {
                try {
                    chunkFile.delete();
                } catch (Exception e) {
                    log.debug("临时分块文件删除错误：{}", e.getMessage());
                }
            }
            try {
                mergeFile.delete();
            } catch (Exception e) {
                log.debug("临时合并文件删除错误：{}", e.getMessage());
            }
        }
    }

    /**
     *  下载分块
     *
     * @param fileMd5
     * @param chunkTotal 分块数量
     */
    private File[] checkChunkStatus(String fileMd5, int chunkTotal) {
        // 作为结果返回
        File[] files = new File[chunkTotal];
        // 获取分块文件目录
        String chunkFileFolder = getChunkFileFolderPath(fileMd5);
        for (int i = 0; i < chunkTotal; i++) {
            // 获取分块文件路径
            String chunkFilePath = chunkFileFolder + i;
            File chunkFile = null;
            try {
                // 创建临时的分块文件
                chunkFile = File.createTempFile("chunk" + i, null);
            } catch (Exception e) {
                BusinessException.cast("创建临时分块文件出错：" + e.getMessage());
            }
            // 下载分块文件
            chunkFile = downloadFileFromMinIO(chunkFile, bucketVideo, chunkFilePath);
            // 组成结果
            files[i] = chunkFile;
        }
        return files;
    }

    /**
     * 合并后的文件地址
     *
     * @param fileMd5 文件id，即MD5值
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePathByMd5(String fileMd5,String fileExt){
        return fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }

    /**
     * 原始downloadFileFromMinIO
     * 根据桶和文件路径从minio下载文件
     *
     * @param bucket
     * @param objectName
     * @return
     */
    public File downloadFileFromMinIOOld(String bucket, String objectName){
        // 临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;

        try {
            InputStream stream = minioClient.getObject(GetObjectArgs
                .builder()
                .bucket(bucket)
                .object(objectName)
                .build());
            // 创建临时文件
            minioFile = File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream, outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 新版本  根据桶和文件路径从minio下载文件
     *
     * @param file
     * @param bucket
     * @param objectName
     * @return
     */
    public File downloadFileFromMinIO(File file,String bucket,String objectName){

        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucket).object(objectName).build();
        try(
                InputStream inputStream = minioClient.getObject(getObjectArgs);
                FileOutputStream outputStream = new FileOutputStream(file);
        ) {
            IOUtils.copy(inputStream,outputStream);
            return file;
        }catch (Exception e){
            e.printStackTrace();
            BusinessException.cast("查询分块文件出错");
        }
        return null;
    }

    /**
     * 清理分块文件
     *
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal 分块文件总数
     */
    private void clearChunkFiles(String chunkFileFolderPath, int chunkTotal) {
        Iterable<DeleteObject> objects = Stream
                .iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> new DeleteObject(chunkFileFolderPath + Integer.toString(i)))
                .collect(Collectors.toList());
        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(bucketVideo).objects(objects).build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);

        // 真正删除
        results.forEach(f -> {
            try {
                DeleteError deleteError = f.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
