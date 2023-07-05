package com.cyan.springcloud.content.feignclient;

import org.springframework.web.multipart.MultipartFile;

/**
 * MediaServiceClientFallback
 *
 * @author Cyan Chau
 * @create 2023-07-05
 */
public class MediaServiceClientFallback implements MediaServiceClient {

    @Override
    public String upload(MultipartFile filedata, String objectName) {
        return null;
    }
}
