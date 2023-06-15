package com.cyan.springcloud.media.service;

import com.cyan.springcloud.base.model.PageParams;
import com.cyan.springcloud.base.model.PageResult;
import com.cyan.springcloud.media.model.dto.QueryMediaParamsDto;
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
}
