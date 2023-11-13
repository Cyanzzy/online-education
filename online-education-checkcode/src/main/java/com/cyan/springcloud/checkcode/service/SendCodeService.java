package com.cyan.springcloud.checkcode.service;

/**
 * @author Cyan Chau
 * @create 2023-07-07
 */
public interface SendCodeService {

    /**
     * 向目标邮箱发送验证码
     *
     * @param email 目标邮箱
     * @param code  我们发送的验证码
     */
    void sendEMail(String email, String code);
}
