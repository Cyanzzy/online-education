package com.cyan.springcloud.ucenter.service.impl;

import com.cyan.springcloud.ucenter.model.dto.AuthParamsDto;
import com.cyan.springcloud.ucenter.model.dto.XcUserExt;
import com.cyan.springcloud.ucenter.service.AuthService;
import org.springframework.stereotype.Service;

/**
 * 微信扫码验证
 *
 * @author Cyan Chau
 * @create 2023-07-07
 */
@Service("wechatAuthService")
public class WechatAuthServiceImpl implements AuthService {

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        return null;
    }
}
