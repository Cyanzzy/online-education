package com.cyan.springcloud.ucenter.service;

import com.cyan.springcloud.ucenter.model.dto.AuthParamsDto;
import com.cyan.springcloud.ucenter.model.dto.OlUserExt;

/**
 * 统一认证接口
 *
 * @author Cyan Chau
 * @create 2023-07-07
 */
public interface AuthService {

    /**
     * 认证方法
     *
     * @param authParamsDto 认证参数
     */
    OlUserExt execute(AuthParamsDto authParamsDto);
}
