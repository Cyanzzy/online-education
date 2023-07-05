package com.cyan.springcloud.content;

import com.cyan.springcloud.content.config.MultipartSupportConfig;
import com.cyan.springcloud.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * 远程调用测试
 *
 * @author Cyan Chau
 * @create 2023-07-05
 */
@SpringBootTest
public class FeignUploadTest {

    @Autowired
    private MediaServiceClient mediaServiceClient;

    @Test
    public void testUpload() throws IOException {
        File file = new File("D:\\data\\upload\\1.html");
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);

        mediaServiceClient.upload(multipartFile, "course/1.html");
    }

}
