package com.cyan.springcloud.content.service.impl;

import com.cyan.springcloud.content.mapper.CourseCategoryMapper;
import com.cyan.springcloud.content.service.CourseCategoryService;
import com.cyan.springcloud.model.dto.CourseCategoryTreeDto;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程分类
 *
 * @author Cyan Chau
 * @create 2023-02-10
 */
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Resource
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        // 调用mapper递归查询分类信息
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);

        // 找到每个节点的子节点，最终封装成List<CourseCategoryTreeDto>
        // 将List转Map，key：节点id，value：CourseCategoryTreeDto对象
        Map<String, CourseCategoryTreeDto> treeDtoMap = courseCategoryTreeDtos
                .stream()
                .filter(item->!id.equals(item.getId()))
                .collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));
        // 定义返回对象
        List<CourseCategoryTreeDto> list = new ArrayList<>();
        // 遍历courseCategoryTreeDtos，将其中子节点放入父节点的childrenTreeNodes
        courseCategoryTreeDtos.stream().filter(item->!id.equals(item.getId())).forEach(item->{
            if (item.getParentid().equals(id)) { // 存入二级节点
                list.add(item);
            }
            // 处理三级节点
            // 找到节点的父亲节点
            CourseCategoryTreeDto parentNode = treeDtoMap.get(item.getParentid());
            if (parentNode != null) {
                if (parentNode.getChildrenTreeNodes() == null) {
                    parentNode.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                parentNode.getChildrenTreeNodes().add(item);
            }

        });

        return list;
    }
}
