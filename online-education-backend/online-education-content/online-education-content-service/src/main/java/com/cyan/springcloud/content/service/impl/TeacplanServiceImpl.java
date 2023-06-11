package com.cyan.springcloud.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.springcloud.content.mapper.TeachplanMapper;
import com.cyan.springcloud.content.service.TeacplanService;
import com.cyan.springcloud.model.dto.SaveTeachplanDto;
import com.cyan.springcloud.model.dto.TeachplanDto;
import com.cyan.springcloud.model.po.Teachplan;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 课程计划管理逻辑接口实现
 *
 * @author Cyan Chau
 * @create 2023-06-11
 */
@Service
public class TeacplanServiceImpl implements TeacplanService {

    @Resource
    private TeachplanMapper teachplanMapper;

    @Override
    public List<TeachplanDto> selectTreeNodes(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {

        Long teachplanId = saveTeachplanDto.getId();

        if (teachplanId == null) { // 新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            // 确定排序字段，即找到它的同级节点个数，然后+1
            // SELECT COUNT(1) FROM teachplan WHERE course_id = 117 AND parent_id = 268
            Long parentid = saveTeachplanDto.getParentid();
            Long courseId = saveTeachplanDto.getCourseId();

            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, parentid);
            Integer count = teachplanMapper.selectCount(queryWrapper);
            teachplan.setOrderby(count + 1);

            teachplanMapper.insert(teachplan);
        } else { // 修改
            Teachplan teachplan = teachplanMapper.selectById(teachplanId);
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        }

    }
}
