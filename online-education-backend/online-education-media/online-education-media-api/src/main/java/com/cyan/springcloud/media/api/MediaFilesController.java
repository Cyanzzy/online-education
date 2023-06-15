package com.cyan.springcloud.media.api;

import com.cyan.springcloud.base.model.PageParams;
import com.cyan.springcloud.base.model.PageResult;
import com.cyan.springcloud.media.model.dto.QueryMediaParamsDto;
import com.cyan.springcloud.media.model.dto.UploadFileParamsDto;
import com.cyan.springcloud.media.model.dto.UploadFileResultDto;
import com.cyan.springcloud.media.model.po.MediaFiles;
import com.cyan.springcloud.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * 媒资文件管理控制层
 *
 * @author Cyan Chau
 * @create 2023-06-15
 */
@Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {

    @Resource
    private MediaFileService mediaFileService;

    @PostMapping("/files")
    @ApiOperation("媒资列表查询接口")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto){
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiles(companyId,pageParams,queryMediaParamsDto);
    }

    @ApiOperation("上传图片")
    @RequestMapping(value = "/upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile filedata) throws IOException {
        // 准备上传的文件信息
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        // 原始文件名称
        uploadFileParamsDto.setFilename(filedata.getOriginalFilename());
        // 文件大小
        uploadFileParamsDto.setFileSize(filedata.getSize());
        // 文件类型
        uploadFileParamsDto.setFileType("001001");
        // 创建临时文件
        File tempFile = File.createTempFile("minio", ".temp");
        filedata.transferTo(tempFile);
        Long companyId = 1232141425L;
        // 文件路径
        String localFilePath = tempFile.getAbsolutePath();
        // 调用service上传图片
        return mediaFileService.uploadFile(companyId, uploadFileParamsDto, localFilePath);
    }


}
