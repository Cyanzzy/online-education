package com.cyan.springcloud.ucenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.cyan.springcloud.ucenter.model.po.OlMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OlMenuMapper extends BaseMapper<OlMenu> {
//    @Select("SELECT	* FROM xc_menu WHERE id IN (SELECT menu_id FROM xc_permission WHERE role_id IN ( SELECT role_id FROM xc_user_role WHERE user_id = #{userId} ))")
    List<OlMenu> selectPermissionByUserId(@Param("userId") String userId);
}
