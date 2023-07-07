package com.cyan.springcloud.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 统一了认证入口， 重写DaoAuthenticationProvider方法，有些认证方式不需要校验密码
 *
 * @author Cyan Chau
 * @create 2023-07-07
 */
@Component
public class DaoAuthenticationProviderCustom extends DaoAuthenticationProvider {

    @Resource
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        super.setUserDetailsService(userDetailsService);
    }

    // 屏蔽密码对比
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        super.additionalAuthenticationChecks(userDetails, authentication);
    }
}
