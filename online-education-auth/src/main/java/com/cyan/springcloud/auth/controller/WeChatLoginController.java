package com.cyan.springcloud.auth.controller;

import com.cyan.springcloud.ucenter.model.po.OlUser;
import com.cyan.springcloud.ucenter.service.WechatAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

/**
 * 微信登录接口
 *
 * @author Cyan Chau
 * @create 2023-07-07
 */
@Slf4j
@Controller
public class WeChatLoginController {

    @Resource
    private WechatAuthService wechatAuthService;

    @RequestMapping("/wxLogin")
    public String wechatLogin(String code, String state) {
        log.debug("微信扫码回调,code:{},state:{}", code, state);
        // 请求微信申请令牌，拿到令牌查询用户信息，将用户信息写入本项目数据库
        OlUser olUser = wechatAuthService.wechatAuth(code);

        if (olUser == null) {
            return "redirect:http://www.51xuecheng.cn/error.html";
        }
        String username = olUser.getUsername();
        return "redirect:http://www.51xuecheng.cn/sign.html?username=" + username + "&authType=wx";

    }

}
