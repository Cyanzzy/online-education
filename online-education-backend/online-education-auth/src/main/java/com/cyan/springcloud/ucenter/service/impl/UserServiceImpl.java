package com.cyan.springcloud.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.springcloud.ucenter.mapper.XcUserMapper;
import com.cyan.springcloud.ucenter.model.dto.AuthParamsDto;
import com.cyan.springcloud.ucenter.model.dto.XcUserExt;
import com.cyan.springcloud.ucenter.model.po.XcUser;
import com.cyan.springcloud.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Cyan Chau
 * @create 2023-07-07
 */
@Slf4j
@Component
public class UserServiceImpl implements UserDetailsService {

    @Resource
    private XcUserMapper xcUserMapper;

    @Resource
    private ApplicationContext applicationContext;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        // 将传入的Json数据转换成对象
        AuthParamsDto authParamsDto = null;

        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求参数不符合要求：{}", s);
           throw new RuntimeException("认证请求参数不符合要求");
        }

        // 认证类型
        String authType = authParamsDto.getAuthType();
        // 根据认证类型从Spring容器中取出指定的Bean
        String beanName = authType + "AuthService";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        // 调用统一认证
        XcUserExt xcUserExt = authService.execute(authParamsDto);

        return getUserPrincipal(xcUserExt);
    }

    /**
     * 查询用户信息
     *
     * @param user 用户主键
     * @return
     */
    public UserDetails getUserPrincipal(XcUserExt user) {

        String password = user.getPassword();
        // 权限
        String[] authorities = {"p1"};
        user.setPassword(null);
        String userJson = JSON.toJSONString(user);
        UserDetails userDetails = User.withUsername(userJson).password(password).authorities(authorities).build();

        return userDetails;
    }
}
