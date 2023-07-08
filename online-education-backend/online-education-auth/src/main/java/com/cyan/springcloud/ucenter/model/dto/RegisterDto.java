package com.cyan.springcloud.ucenter.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册请求模型类
 *
 * @author Cyan Chau
 * @create 2023-07-08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {

    private String cellphone;

    private String checkcode;

    private String checkcodekey;

    private String confirmpwd;

    private String email;

    private String nickname;

    private String password;

    private String username;
}
