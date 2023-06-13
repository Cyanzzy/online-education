package com.cyan.springcloud.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.springcloud.base.exception.BusinessException;
import com.cyan.springcloud.content.mapper.TeachplanMapper;
import com.cyan.springcloud.content.mapper.TeachplanMediaMapper;
import com.cyan.springcloud.content.service.TeacplanService;
import com.cyan.springcloud.model.dto.SaveTeachplanDto;
import com.cyan.springcloud.model.dto.TeachplanDto;
import com.cyan.springcloud.model.po.Teachplan;
import com.cyan.springcloud.model.po.TeachplanMedia;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Resource
    private TeachplanMediaMapper teachplanMediaMapper;

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

    @Override
    @Transactional
    public void deleteTeachplan(Long teachPlanId) {
        if (teachPlanId == null) {
            BusinessException.cast("课程计划Id不存在");
        }
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        // SELECT * FROM teachplan WHERE parentid = {当前章计划id}
        queryWrapper.eq(Teachplan::getParentid, teachPlanId);
        // 获取查询的条目
        Integer count = teachplanMapper.selectCount(queryWrapper);
        if (count > 0 ) {
            BusinessException.cast("课程计划信息还有子级信息，无法操作");
        } else { // 课程计划信息下没有子级信息，可以直接删除课程计划信息和对应的媒资信息
            teachplanMapper.deleteById(teachPlanId);
            LambdaQueryWrapper<TeachplanMedia> mediaLambdaQueryWrapper = new LambdaQueryWrapper<>();
            // 删除媒资信息中对应的teachplanId的数据
            mediaLambdaQueryWrapper.eq(TeachplanMedia::getTeachplanId, teachPlanId);
            teachplanMediaMapper.delete(mediaLambdaQueryWrapper);
        }
    }
}
