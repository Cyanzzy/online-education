package com.cyan.springcloud.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.springcloud.ucenter.mapper.XcUserMapper;
import com.cyan.springcloud.ucenter.mapper.XcUserRoleMapper;
import com.cyan.springcloud.ucenter.model.dto.FindPswDto;
import com.cyan.springcloud.ucenter.model.dto.RegisterDto;
import com.cyan.springcloud.ucenter.model.po.XcUser;
import com.cyan.springcloud.ucenter.model.po.XcUserRole;
import com.cyan.springcloud.ucenter.service.VerifyService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.UUID;


/**
 * @author Cyan Chau
 * @create 2023-07-07
 */
@Service
public class VerifyServiceImpl implements VerifyService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private XcUserMapper xcUserMapper;

    @Resource
    private XcUserRoleMapper xcUserRoleMapper;

    public Boolean verifyCheckCode(String email, String checkcode) {
        // 1. 从redis中获取缓存的验证码
        String codeInRedis = stringRedisTemplate.opsForValue().get(email);
        // 2. 判断是否与用户输入的一致
        if (codeInRedis.equalsIgnoreCase(checkcode)) {
            stringRedisTemplate.delete(email);
            return true;
        }
        return false;
    }

    @Override
    public void findPassword(FindPswDto findPswDto) {
        String email = findPswDto.getEmail();
        String checkcode = findPswDto.getCheckcode();
        Boolean verifyCheckCode = verifyCheckCode(email, checkcode);
        if (!verifyCheckCode) {
            throw new RuntimeException("验证码输入错误");
        }
        String password = findPswDto.getPassword();
        String confirmpwd = findPswDto.getConfirmpwd();
        if (!password.equals(confirmpwd)) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        LambdaQueryWrapper<XcUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(XcUser::getEmail, findPswDto.getEmail());
        XcUser user = xcUserMapper.selectOne(lambdaQueryWrapper);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        xcUserMapper.updateById(user);
    }

    @Override
    @Transactional
    public void register(RegisterDto registerDto) {
        String uuid = UUID.randomUUID().toString();
        String email = registerDto.getEmail();
        String checkcode = registerDto.getCheckcode();
        Boolean verifyCheckCode = verifyCheckCode(email, checkcode);
        if (!verifyCheckCode) {
            throw new RuntimeException("验证码输入错误");
        }
        String password = registerDto.getPassword();
        String confirmpwd = registerDto.getConfirmpwd();
        if (!password.equals(confirmpwd)) {
            throw new RuntimeException("两次输入密码不一致");
        }
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(XcUser::getEmail, registerDto.getEmail());
        XcUser user = xcUserMapper.selectOne(queryWrapper);

        if (user != null) {
            throw new RuntimeException("用户已经存在，一个邮箱只能注册一个账户");
        }
        XcUser xcUser = new XcUser();
        BeanUtils.copyProperties(registerDto, xcUser);
        xcUser.setPassword(new BCryptPasswordEncoder().encode(password));
        xcUser.setId(uuid);
        xcUser.setUtype("101001");  // 学生类型
        xcUser.setStatus("1");
        xcUser.setName(registerDto.getNickname());
        xcUser.setCreateTime(LocalDateTime.now());
        int xcUserInsert = xcUserMapper.insert(xcUser);
        if (xcUserInsert <= 0) {
            throw new RuntimeException("新增用户信息失败");
        }
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(uuid);
        xcUserRole.setUserId(uuid);
        xcUserRole.setRoleId("17");
        xcUserRole.setCreateTime(LocalDateTime.now());
        int xcUserRoleInsert = xcUserRoleMapper.insert(xcUserRole);
        if (xcUserRoleInsert <= 0) {
            throw new RuntimeException("新增用户角色信息失败");
        }

    }
}
