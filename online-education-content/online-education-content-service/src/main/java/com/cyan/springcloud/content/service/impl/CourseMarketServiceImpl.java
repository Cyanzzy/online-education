package com.cyan.springcloud.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyan.springcloud.content.mapper.CourseMarketMapper;
import com.cyan.springcloud.content.service.CourseMarketService;
import com.cyan.springcloud.model.po.CourseMarket;
import org.springframework.stereotype.Service;

/**
 * @author Cyan Chau
 * @create 2023-06-09
 */
@Service
public class CourseMarketServiceImpl extends ServiceImpl<CourseMarketMapper, CourseMarket> implements CourseMarketService {
}
