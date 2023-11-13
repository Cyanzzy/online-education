package com.cyan.springcloud.media.api;

import com.cyan.springcloud.base.exception.BusinessException;
import com.cyan.springcloud.base.model.RestResponse;
import com.cyan.springcloud.media.model.po.MediaFiles;
import com.cyan.springcloud.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 媒资文件管理接口
 *
 * @author Cyan Chau
 * @create 2023-07-04
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
@RequestMapping("/open")
public class MediaOpenController {

    @Resource
    private MediaFileService mediaFileService;

    @ApiOperation("预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId){

        MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);
        if(mediaFiles == null || StringUtils.isEmpty(mediaFiles.getUrl())){
            BusinessException.cast("视频还没有转码处理");
        }
        return RestResponse.success(mediaFiles.getUrl());
    }

}
