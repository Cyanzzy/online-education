package com.cyan.springcloud.content.feignclient;

import com.cyan.springcloud.content.config.MultipartSupportConfig;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * 远程调用媒资服务接口
 *
 * @author Cyan Chau
 * @create 2023-07-05
 */
@RequestMapping("/media")
@FeignClient(value = "media-api", configuration = {MultipartSupportConfig.class}, fallbackFactory = MediaServiceClientFallbackFactory.class)
public interface MediaServiceClient {

    @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String upload(@RequestPart("filedata") MultipartFile filedata,
                  @RequestParam(value = "objectName", required = false) String objectName);
}
