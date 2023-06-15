package com.cyan.springcloud.media.service;

import com.cyan.springcloud.base.model.PageParams;
import com.cyan.springcloud.base.model.PageResult;
import com.cyan.springcloud.media.model.dto.QueryMediaParamsDto;
import com.cyan.springcloud.media.model.dto.UploadFileParamsDto;
import com.cyan.springcloud.media.model.dto.UploadFileResultDto;
import com.cyan.springcloud.media.model.po.MediaFiles;

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

}
