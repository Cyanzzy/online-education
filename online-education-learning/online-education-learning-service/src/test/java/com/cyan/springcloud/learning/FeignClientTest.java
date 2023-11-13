package com.cyan.springcloud.learning;

import com.cyan.springcloud.learning.feignclient.ContentServiceClient;
import com.cyan.springcloud.model.po.CoursePublish;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author Cyan Chau
 * @create 2023-07-08
 */
@SpringBootTest
public class FeignClientTest {

    @Resource
    private ContentServiceClient contentServiceClient;

    @Test
    public void testContentServiceClient(){
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(117L);
        System.out.println(coursepublish);
        Assertions.assertNotNull(coursepublish);
    }

}
