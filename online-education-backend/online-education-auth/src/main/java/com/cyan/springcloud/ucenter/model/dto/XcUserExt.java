package com.cyan.springcloud.ucenter.model.dto;

import com.cyan.springcloud.ucenter.model.po.XcUser;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户扩展信息
 */
@Data
public class XcUserExt extends XcUser {
    // 用户权限
    List<String> permissions = new ArrayList<>();
}
