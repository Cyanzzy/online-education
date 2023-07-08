package com.cyan.springcloud.checkcode.service.impl;

import com.cyan.springcloud.base.exception.BusinessException;
import com.cyan.springcloud.checkcode.service.SendCodeService;
import com.cyan.springcloud.checkcode.utils.MailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.concurrent.TimeUnit;

/**
 * @author Cyan Chau
 * @create 2023-07-07
 */
@Slf4j
@Service
public class SendCodeServiceImpl implements SendCodeService {

    public final Long CODE_TTL = 120L;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void sendEMail(String email, String code) {
        // 1. 向用户发送验证码
        try {
            MailUtil.sendTestMail(email, code);
        } catch (MessagingException e) {
            log.info("邮件发送失败：{}", e.getMessage());
            BusinessException.cast("发送验证码失败，请稍后再试");
        }
        // 2. 将验证码缓存到redis，TTL设置为2分钟
        stringRedisTemplate.opsForValue().set(email, code, CODE_TTL, TimeUnit.SECONDS);
    }
}
