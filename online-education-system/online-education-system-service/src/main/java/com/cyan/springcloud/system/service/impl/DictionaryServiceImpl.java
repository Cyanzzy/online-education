package com.cyan.springcloud.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyan.springcloud.system.mapper.DictionaryMapper;
import com.cyan.springcloud.system.model.po.Dictionary;
import com.cyan.springcloud.system.service.DictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据字典服务类
 *
 * @author CyanChau
 * @date 2023-01-26
 */
@Slf4j
@Service
public class DictionaryServiceImpl extends ServiceImpl<DictionaryMapper, Dictionary> implements DictionaryService {

    @Override
    public List<Dictionary> queryAll() {

        List<Dictionary> list = this.list();

        return list;
    }

    @Override
    public Dictionary getByCode(String code) {

        LambdaQueryWrapper<Dictionary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dictionary::getCode, code);

        Dictionary dictionary = this.getOne(queryWrapper);

        return dictionary;
    }
}
