package com.cyan.springcloud.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 搜索服务降级处理
 *
 * @author Cyan Chau
 * @create 2023-07-06
 */
@Slf4j
@Component
public class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {

    @Override
    public SearchServiceClient create(Throwable throwable) {
        return new SearchServiceClient() {
            @Override
            public Boolean add(CourseIndex courseIndex) {
                log.info("添加课程索引发生熔断，索引信息：{}，熔断异常：{}", courseIndex, throwable.toString(), throwable);
                return false;
            }
        };
    }
}
