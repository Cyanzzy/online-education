package com.cyan.springcloud.content.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 远程调用搜索服务接口
 *
 * @author Cyan Chau
 * @create 2023-07-06
 */
@RequestMapping("/search")
@FeignClient(value = "search", fallbackFactory = SearchServiceClientFallbackFactory.class)
public interface SearchServiceClient {

    @PostMapping("/index/course")
    Boolean add(@RequestBody CourseIndex courseIndex);
}