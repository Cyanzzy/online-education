package com.cyan.springcloud.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Cyan Chau
 * @create 2023-07-05
 */
@Slf4j
@Component
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {

    @Override
    public MediaServiceClient create(Throwable throwable) {
        // 发生熔断，上游服务调用此方法进行服务降级
        return new MediaServiceClient() {
            @Override
            public String upload(MultipartFile filedata, String objectName) {
                log.debug("调用媒资管理服务上传文件时发生熔断，异常信息:{}", throwable.toString(), throwable);
                return null;
            }
        };

    }
}
