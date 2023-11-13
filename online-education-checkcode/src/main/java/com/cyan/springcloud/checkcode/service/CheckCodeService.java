package com.cyan.springcloud.checkcode.service;


import com.cyan.springcloud.checkcode.model.CheckCodeParamsDto;
import com.cyan.springcloud.checkcode.model.CheckCodeResultDto;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 验证码接口
 */
public interface CheckCodeService {


    /**
     * 生成验证码
     *
     * @param checkCodeParamsDto 生成验证码参数
     */
    CheckCodeResultDto generate(CheckCodeParamsDto checkCodeParamsDto);

    /**
     * 校验验证码
     *
     * @param key
     * @param code
     */
    boolean verify(String key, String code);


    /**
     * 验证码生成器
     */
    interface CheckCodeGenerator {
        /**
         * 验证码生成
         *
         * @return 验证码
         */
        String generate(int length);

    }

    /**
     * key生成器
     */
    interface KeyGenerator {

        /**
         * key生成
         *
         * @return 验证码
         */
        String generate(String prefix);
    }


    /**
     * 验证码存储
     */
    interface CheckCodeStore {

        /**
         * 向缓存设置key
         *
         * @param key    key
         * @param value  value
         * @param expire 过期时间,单位秒
         */
        void set(String key, String value, Integer expire);

        String get(String key);

        void remove(String key);
    }
}
