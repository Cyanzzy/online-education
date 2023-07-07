package com.cyan.springcloud.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.springcloud.ucenter.mapper.XcUserMapper;
import com.cyan.springcloud.ucenter.model.dto.AuthParamsDto;
import com.cyan.springcloud.ucenter.model.dto.XcUserExt;
import com.cyan.springcloud.ucenter.model.po.XcUser;
import com.cyan.springcloud.ucenter.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 账号密码方式认证
 *
 * @author Cyan Chau
 * @create 2023-07-07
 */
@Service("passwordAuthService")
public class PasswordAuthServiceImpl implements AuthService {

    @Resource
    private XcUserMapper xcUserMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {

        // 账号
        String username = authParamsDto.getUsername();

        // TODO 校验验证码

        // *********账户是否存在
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (xcUser == null) {
            throw new RuntimeException("账户不存在");
        }

        // ***********验证密码是否正确
        // 如果用户存在，获取数据库中的密码
        String passwordFromDb = xcUser.getPassword();
        // 获取用户输入的密码
        String passwordFromWeb = authParamsDto.getPassword();
        // 校验密码
        boolean matches = passwordEncoder.matches(passwordFromWeb, passwordFromDb);
        if (!matches) {
            throw new RuntimeException("账户或密码错误");
        }

        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);

        return xcUserExt;
    }
}
