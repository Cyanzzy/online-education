package com.cyan.springcloud.ucenter.service;

import com.cyan.springcloud.ucenter.model.po.XcUser;

/**
 * 微信认证
 *
 * @author Cyan Chau
 * @create 2023-07-07
 */
public interface WechatAuthService {

    /**
     * 微信扫码认证
     *  1. 申请令牌
     *  2. 携带令牌查询用户信息
     *  3. 保存用户信息到数据库
     * @param code
     * @return
     */
    XcUser wechatAuth(String code);
}
