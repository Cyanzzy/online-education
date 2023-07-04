package com.cyan.springcloud.media.service;

import com.cyan.springcloud.base.model.PageParams;
import com.cyan.springcloud.base.model.PageResult;
import com.cyan.springcloud.base.model.RestResponse;
import com.cyan.springcloud.media.model.dto.QueryMediaParamsDto;
import com.cyan.springcloud.media.model.dto.UploadFileParamsDto;
import com.cyan.springcloud.media.model.dto.UploadFileResultDto;
import com.cyan.springcloud.media.model.po.MediaFiles;
import io.minio.GetObjectArgs;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

/**
 * 媒资文件管理业务接口
 *
 * @author Cyan Chau
 * @create 2023-06-15
 */
public interface MediaFileService {
    /**
     * 媒资文件查询方法
     *
     * @param pageParams 分页参数
     * @param queryMediaParamsDto 查询条件
     * @return
     */
    PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    /**
     * 上传文件方法
     *
     * @param companyId 机构id
     * @param uploadFileParamsDto 文件信息
     * @param localFilePath 文件本地路径
     * @return
     */
    UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath);

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

    MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName);

    /**
     * 检查文件是否存在
     *
     * @param fileMd5 文件的md5
     */
    RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * 检查分块是否存在
     *
     * @param fileMd5  文件的md5
     * @param chunkIndex  分块序号
     */
    RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

    /**
     * 上传分块
     *
     * @param fileMd5  文件md5
     * @param chunk  分块序号
     * @param localChunkFilePath  文件路径
     */
    RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath);

    /**
     * 合并分块
     *
     * @param companyId 机构id
     * @param fileMd5 文件md5
     * @param chunkTotal 文件总和
     * @param uploadFileParamsDto 文件信息
     * @return
     */
    RestResponse mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) throws IOException;

    /**
     * 原始方法 有BUG
     *
     * @param companyId 机构id
     * @param fileMd5 文件md5
     * @param chunkTotal 文件总和
     * @param uploadFileParamsDto 文件信息
     * @return
     */
    RestResponse mergeChunksOld(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto);

    /**
     * 原始downloadFileFromMinIO
     * 根据桶和文件路径从minio下载文件
     *
     * @param bucket
     * @param objectName
     * @return
     */
    File downloadFileFromMinIOOld(String bucket, String objectName);

    /**
     * 将文件上传到minio
     *
     * @param localFilePath 本地文件路径
     * @param mimeType      媒体类型
     * @param bucket        桶
     * @param objectName    对象
     * @return
     */
    boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName);

    /**
     * 根据媒资id获取媒资文件
     *
     * @param mediaId 媒资id
     * @return
     */
    MediaFiles getFileById(String mediaId);
}
