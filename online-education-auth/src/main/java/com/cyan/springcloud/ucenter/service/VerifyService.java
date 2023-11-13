package com.cyan.springcloud.ucenter.service;

import com.cyan.springcloud.ucenter.model.dto.FindPswDto;
import com.cyan.springcloud.ucenter.model.dto.RegisterDto;

/**
 * @author Cyan Chau
 * @create 2023-07-07
 */
public interface VerifyService {

    /**
     * 找回密码
     *
     * @param findPswDto
     */
    void findPassword(FindPswDto findPswDto);

    /***
     * 注册
     *
     * @param registerDto
     */
    void register(RegisterDto registerDto);
}
