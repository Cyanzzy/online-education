package com.cyan.springcloud.ucenter.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用于接收找回密码的参数信息
 *
 * @author Cyan Chau
 * @create 2023-07-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindPswDto {

    String cellphone;

    String email;

    String checkcodekey;

    String checkcode;

    String password;

    String confirmpwd;
}