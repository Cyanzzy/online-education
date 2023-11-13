package com.cyan.springcloud.auth.controller;

import com.cyan.springcloud.ucenter.mapper.XcUserMapper;
import com.cyan.springcloud.ucenter.model.dto.FindPswDto;
import com.cyan.springcloud.ucenter.model.dto.RegisterDto;
import com.cyan.springcloud.ucenter.model.po.XcUser;
import com.cyan.springcloud.ucenter.service.VerifyService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
public class LoginController {

    @Resource
    private XcUserMapper userMapper;

    @Resource
    private VerifyService verifyService;


    @RequestMapping("/login-success")
    public String loginSuccess() {

        return "登录成功";
    }


    @RequestMapping("/user/{id}")
    public XcUser getuser(@PathVariable("id") String id) {
        XcUser xcUser = userMapper.selectById(id);
        return xcUser;
    }

    @RequestMapping("/r/r1")
    public String r1() {
        return "访问r1资源";
    }

    @RequestMapping("/r/r2")
    public String r2() {
        return "访问r2资源";
    }


    @ApiOperation(value = "找回密码", tags = "找回密码")
    @PostMapping("/findpassword")
    public void findPassword(@RequestBody FindPswDto findPswDto) {
        verifyService.findPassword(findPswDto);
    }

    @ApiOperation(value = "注册", tags = "注册")
    @PostMapping("/register")
    public void register(@RequestBody RegisterDto registerDto) {

         // TODO 本项目采用邮箱发送验证和校验验证码，手机发送验证码和校验验证码尚未开发，测试注册功能时手机号栏目请填写邮箱，便于测试
        verifyService.register(registerDto);
    }
}
