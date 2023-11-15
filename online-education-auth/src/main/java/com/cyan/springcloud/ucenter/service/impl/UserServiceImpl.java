package com.cyan.springcloud.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.cyan.springcloud.ucenter.mapper.OlMenuMapper;
import com.cyan.springcloud.ucenter.mapper.OlUserMapper;
import com.cyan.springcloud.ucenter.model.dto.AuthParamsDto;
import com.cyan.springcloud.ucenter.model.dto.OlUserExt;
import com.cyan.springcloud.ucenter.model.po.OlMenu;
import com.cyan.springcloud.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cyan Chau
 * @create 2023-07-07
 */
@Slf4j
@Component
public class UserServiceImpl implements UserDetailsService {

    @Resource
    private OlUserMapper olUserMapper;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private OlMenuMapper olMenuMapper;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        // 将传入的Json数据转换成对象
        AuthParamsDto authParamsDto = null;

        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求参数不符合要求：{}", s);
           throw new RuntimeException("认证请求参数不符合要求");
        }

        // 认证类型
        String authType = authParamsDto.getAuthType();
        // 根据认证类型从Spring容器中取出指定的Bean
        String beanName = authType + "AuthService";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        // 调用统一认证
        OlUserExt xcUserExt = authService.execute(authParamsDto);

        return getUserPrincipal(xcUserExt);
    }

    /**
     * 查询用户信息
     *
     * @param user 用户主键
     * @return
     */
    public UserDetails getUserPrincipal(OlUserExt user) {
        String password = user.getPassword();
        // 权限
        String[] authorities = {"p1"};
        // 根据用户id查询用户权限
        List<OlMenu> olMenus = olMenuMapper.selectPermissionByUserId(user.getId());
        if (olMenus.size() > 0) {
            List<String> permissions = new ArrayList<>();
            olMenus.forEach(olMenu -> {
                // 用户拥有权限的标识符
                permissions.add(olMenu.getCode());
            });
            authorities = permissions.toArray(new String[0]);
        }

        user.setPassword(null);
        String userJson = JSON.toJSONString(user);
        UserDetails userDetails = User.withUsername(userJson).password(password).authorities(authorities).build();

        return userDetails;
    }
}
