package com.cyan.springcloud.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.springcloud.base.exception.BusinessException;
import com.cyan.springcloud.base.model.PageParams;
import com.cyan.springcloud.base.model.PageResult;
import com.cyan.springcloud.media.mapper.MediaFilesMapper;
import com.cyan.springcloud.media.model.dto.QueryMediaParamsDto;
import com.cyan.springcloud.media.model.dto.UploadFileParamsDto;
import com.cyan.springcloud.media.model.dto.UploadFileResultDto;
import com.cyan.springcloud.media.model.po.MediaFiles;
import com.cyan.springcloud.media.service.MediaFileService;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

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
     * @param mimeType 媒体类型
     * @param bucket 桶
     * @param objectName 对象
     * @return
     */
    private boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .filename(localFilePath)
                    .object(objectName)
                    .contentType(mimeType)
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
        try (FileInputStream fileInputStream = new FileInputStream(file)){
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
     * @param companyId 机构id
     * @param fileMd5 文件md5值
     * @param uploadFileParamsDto 上传文件的信息
     * @param bucket  桶
     * @param objectName 对象
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
            return mediaFiles;
        }
        return mediaFiles;
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
}
