package com.cyan.springcloud.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.springcloud.ucenter.mapper.OlUserMapper;
import com.cyan.springcloud.ucenter.mapper.OlUserRoleMapper;
import com.cyan.springcloud.ucenter.model.dto.FindPswDto;
import com.cyan.springcloud.ucenter.model.dto.RegisterDto;
import com.cyan.springcloud.ucenter.model.po.OlUser;
import com.cyan.springcloud.ucenter.model.po.OlUserRole;
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
    private OlUserMapper olUserMapper;

    @Resource
    private OlUserRoleMapper olUserRoleMapper;

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
        LambdaQueryWrapper<OlUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(OlUser::getEmail, findPswDto.getEmail());
        OlUser user = olUserMapper.selectOne(lambdaQueryWrapper);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        olUserMapper.updateById(user);
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
        LambdaQueryWrapper<OlUser> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(OlUser::getEmail, registerDto.getEmail());
        OlUser user = olUserMapper.selectOne(queryWrapper);

        if (user != null) {
            throw new RuntimeException("用户已经存在，一个邮箱只能注册一个账户");
        }
        OlUser olUser = new OlUser();
        BeanUtils.copyProperties(registerDto, olUser);
        olUser.setPassword(new BCryptPasswordEncoder().encode(password));
        olUser.setId(uuid);
        olUser.setUtype("101001");  // 学生类型
        olUser.setStatus("1");
        olUser.setName(registerDto.getNickname());
        olUser.setCreateTime(LocalDateTime.now());
        int xcUserInsert = olUserMapper.insert(olUser);
        if (xcUserInsert <= 0) {
            throw new RuntimeException("新增用户信息失败");
        }
        OlUserRole olUserRole = new OlUserRole();
        olUserRole.setId(uuid);
        olUserRole.setUserId(uuid);
        olUserRole.setRoleId("17");
        olUserRole.setCreateTime(LocalDateTime.now());
        int xcUserRoleInsert = olUserRoleMapper.insert(olUserRole);
        if (xcUserRoleInsert <= 0) {
            throw new RuntimeException("新增用户角色信息失败");
        }

    }
}
