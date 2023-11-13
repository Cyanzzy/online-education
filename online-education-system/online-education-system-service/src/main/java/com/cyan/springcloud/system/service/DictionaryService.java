package com.cyan.springcloud.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cyan.springcloud.system.model.po.Dictionary;


import java.util.List;

/**
 * 数据字典服务类
 *
 * @author CyanChau
 * @date 2023-01-26
 */
public interface DictionaryService extends IService<Dictionary> {

    /**
     * 查询所有数据字典内容
     * @return
     */
    List<Dictionary> queryAll();

    /**
     * 根据code查询数据字典
     * @param code -- String 数据字典Code
     * @return
     */
    Dictionary getByCode(String code);
}
