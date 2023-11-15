package com.cyan.springcloud.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.springcloud.ucenter.feignclient.CheckCodeClient;
import com.cyan.springcloud.ucenter.mapper.OlUserMapper;
import com.cyan.springcloud.ucenter.model.dto.AuthParamsDto;
import com.cyan.springcloud.ucenter.model.dto.OlUserExt;
import com.cyan.springcloud.ucenter.model.po.OlUser;
import com.cyan.springcloud.ucenter.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
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
    private OlUserMapper olUserMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private CheckCodeClient checkCodeClient;

    @Override
    public OlUserExt execute(AuthParamsDto authParamsDto) {

        // 账号
        String username = authParamsDto.getUsername();

        // 前端输入的验证码
        String checkcode = authParamsDto.getCheckcode();
        // 验证码对应的Key
        String checkcodekey = authParamsDto.getCheckcodekey();
        if (StringUtils.isEmpty(checkcode) || StringUtils.isEmpty(checkcodekey)) {
            throw new RuntimeException("请输入验证码");
        }
        // 远程调用验证码服务 校验验证码
        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);

        if (verify == null || !verify) {
            throw new RuntimeException("验证码输入错误");
        }



        // *********账户是否存在
        OlUser olUser = olUserMapper.selectOne(new LambdaQueryWrapper<OlUser>().eq(OlUser::getUsername, username));
        if (olUser == null) {
            throw new RuntimeException("账户不存在");
        }

        // ***********验证密码是否正确
        // 如果用户存在，获取数据库中的密码
        String passwordFromDb = olUser.getPassword();
        // 获取用户输入的密码
        String passwordFromWeb = authParamsDto.getPassword();
        // 校验密码
        boolean matches = passwordEncoder.matches(passwordFromWeb, passwordFromDb);
        if (!matches) {
            throw new RuntimeException("账户或密码错误");
        }

        OlUserExt olUserExt = new OlUserExt();
        BeanUtils.copyProperties(olUser, olUserExt);

        return olUserExt;
    }
}
