package com.cyan.springcloud.ucenter.service;

import com.cyan.springcloud.ucenter.model.dto.FindPswDto;

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
}
