package com.cyan.springcloud.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.springcloud.ucenter.mapper.XcUserMapper;
import com.cyan.springcloud.ucenter.mapper.XcUserRoleMapper;
import com.cyan.springcloud.ucenter.model.dto.AuthParamsDto;
import com.cyan.springcloud.ucenter.model.dto.XcUserExt;
import com.cyan.springcloud.ucenter.model.po.XcUser;
import com.cyan.springcloud.ucenter.model.po.XcUserRole;
import com.cyan.springcloud.ucenter.service.AuthService;
import com.cyan.springcloud.ucenter.service.WechatAuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 微信扫码验证
 *
 * @author Cyan Chau
 * @create 2023-07-07
 */
@Service("wechatAuthService")
public class WechatAuthServiceImpl implements AuthService, WechatAuthService {

    @Resource
    private XcUserMapper xcUserMapper;

    @Resource
    private XcUserRoleMapper xcUserRoleMapper;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private WechatAuthServiceImpl cuurentProxy;

    @Value("${weixin.appid}")
    private String appid;

    @Value("weixin.secret")
    private String secret;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        //账号
        String username = authParamsDto.getUsername();
        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (user == null) {
            // 返回空表示用户不存在
            throw new RuntimeException("账号不存在");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user, xcUserExt);

        return xcUserExt;
    }

    @Override
    public XcUser wechatAuth(String code) {

        // **********申请令牌
        Map<String, String> accessToken = getAccessToken(code);
        System.out.println(">>>>>>>>>>>>>>accessToekn: " + accessToken);
        // 访问令牌
        String access_token = accessToken.get("access_token");
        // openid
        String openid = accessToken.get("openid");

        // ************携带令牌获取用户信息
        Map<String, String> userinfo = getUserinfo(access_token, openid);

        // *************保存用户信息到数据库
        // 使用代理对象，防止事务失效
        XcUser xcUser = cuurentProxy.addWechatUser(userinfo);
        return xcUser;
    }

    /**
     * 携带授权码申请令牌
     * 响应示例
     * {
     * "access_token":"ACCESS_TOKEN",
     * "expires_in":7200,
     * "refresh_token":"REFRESH_TOKEN",
     * "openid":"OPENID",
     * "scope":"SCOPE",
     * "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * }
     *
     * @param code
     * @return
     */
    private Map<String, String> getAccessToken(String code) {

        // url模板
        String urlTemplate = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        // 最终请求路径
        String url = String.format(urlTemplate, appid, secret, code);

        // 远程调用此url
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
        // 响应结果
        String result = exchange.getBody();
        // result数据转map
        Map<String, String> map = JSON.parseObject(result, Map.class);
        return map;
    }

    /**
     * 携带令牌获取用户信息
     * <p>
     * {
     * "openid":"OPENID",
     * "nickname":"NICKNAME",
     * "sex":1,
     * "province":"PROVINCE",
     * "city":"CITY",
     * "country":"COUNTRY",
     * "headimgurl": "https://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0",
     * "privilege":[
     * "PRIVILEGE1",
     * "PRIVILEGE2"
     * ],
     * "unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * }
     */
    private Map<String, String> getUserinfo(String accessToken, String openid) {

        String urlTemplate = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        String wxUrl = String.format(urlTemplate, accessToken, openid);

        ResponseEntity<String> exchange = restTemplate.exchange(wxUrl, HttpMethod.POST, null, String.class);

        //防止乱码进行转码
        String result = new String(exchange.getBody().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

        Map<String, String> map = JSON.parseObject(result, Map.class);

        return map;
    }

    /**
     * 保存用户信息
     *
     * @param userInfoMap
     * @return
     */
    @Transactional
    public XcUser addWechatUser(Map<String, String> userInfoMap) {
        String unionid = userInfoMap.get("unionid");
        String nickname = userInfoMap.get("nickname");
        // 根据unionid查询用户信息
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getWxUnionid, unionid));
        if (xcUser != null) {
            return xcUser;
        }
        // 新增记录
        xcUser = new XcUser();

        String userId = UUID.randomUUID().toString();
        xcUser.setId(userId);
        xcUser.setUsername(unionid);
        xcUser.setPassword(unionid);
        xcUser.setWxUnionid(unionid);
        xcUser.setNickname(nickname);
        xcUser.setName(nickname);
        xcUser.setUtype("101001"); // 学生类型
        xcUser.setStatus("1"); // 用户状态
        xcUser.setCreateTime(LocalDateTime.now());
        xcUserMapper.insert(xcUser);

        // 用户角色关系表
        XcUserRole xcUserRole = new XcUserRole();

        xcUserRole.setId(UUID.randomUUID().toString());
        xcUserRole.setUserId(userId);
        xcUserRole.setRoleId("17"); // 学生角色
        xcUserRole.setCreateTime(LocalDateTime.now());
        xcUserRoleMapper.insert(xcUserRole);

        return xcUser;
    }
}
