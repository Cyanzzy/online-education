package com.cyan.springcloud.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.springcloud.base.exception.BusinessException;
import com.cyan.springcloud.content.mapper.TeachplanMapper;
import com.cyan.springcloud.content.mapper.TeachplanMediaMapper;
import com.cyan.springcloud.content.service.TeacplanService;
import com.cyan.springcloud.model.dto.BindTeachplanMediaDto;
import com.cyan.springcloud.model.dto.SaveTeachplanDto;
import com.cyan.springcloud.model.dto.TeachplanDto;
import com.cyan.springcloud.model.po.Teachplan;
import com.cyan.springcloud.model.po.TeachplanMedia;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
        if (count > 0) {
            BusinessException.cast("课程计划信息还有子级信息，无法操作");
        } else { // 课程计划信息下没有子级信息，可以直接删除课程计划信息和对应的媒资信息
            teachplanMapper.deleteById(teachPlanId);
            LambdaQueryWrapper<TeachplanMedia> mediaLambdaQueryWrapper = new LambdaQueryWrapper<>();
            // 删除媒资信息中对应的teachplanId的数据
            mediaLambdaQueryWrapper.eq(TeachplanMedia::getTeachplanId, teachPlanId);
            teachplanMediaMapper.delete(mediaLambdaQueryWrapper);
        }
    }

    @Override
    @Transactional
    public void orderByTeachplan(String moveType, Long teachPlanId) {
        // 获取课程计划
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
        // 获取章节级别
        Integer grade = teachplan.getGrade();
        // 获取排序级别
        Integer orderby = teachplan.getOrderby();
        // 大章节移动是比较同一课程id下的orderby
        Long courseId = teachplan.getCourseId();
        // 小章节移动是比较同一章节id下的orderby
        Long parentid = teachplan.getParentid();

        // 向上移动逻辑
        if ("moveup".equals(moveType)) {
            // 大章节
            if (1 == grade) {
                // 大章节上移动，找到上一个大章节orderby，然后与其交换orderby
                // SELECT * FROM teachplan
                // WHERE courseId = #{courseId}
                // AND grade = 1
                // AND orderby < #{orderby}
                // ORDER BY orderby DESC
                // LIMIT 1
                LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Teachplan::getCourseId, courseId)
                        .eq(Teachplan::getGrade, grade)
                        .lt(Teachplan::getOrderby, orderby)
                        .orderByDesc(Teachplan::getOrderby)
                        .last("LIMIT 1");
                Teachplan priorGradeOne = teachplanMapper.selectOne(queryWrapper);
                exchangeOrderby(teachplan, priorGradeOne);
            } else if (2 == grade) { // 小章节
                // 小章节上移动
                // SELECT * FROM teachplan
                // WHERE parentId = #{parentId}
                // AND grade = 2
                // AND orderby < #{orderby}
                // ORDER BY orderby DESC
                // LIMIT 1
                LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Teachplan::getParentid, parentid)
                        .eq(Teachplan::getGrade, grade)
                        .lt(Teachplan::getOrderby, orderby)
                        .orderByDesc(Teachplan::getOrderby)
                        .last("LIMIT 1");
                Teachplan priorGradeTwo = teachplanMapper.selectOne(queryWrapper);
                exchangeOrderby(teachplan, priorGradeTwo);
            }
        }

        // 向下移动逻辑
        if ("movedown".equals(moveType)) {
            // 大章节
            if (1 == grade) {
                // 大章节下移动，找到下一个大章节orderby，然后与其交换orderby
                // SELECT * FROM teachplan
                // WHERE courseId = #{courseId}
                // AND grade = 1
                // AND orderby > #{orderby}
                // ORDER BY orderby ASC
                // LIMIT 1
                LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Teachplan::getCourseId, courseId)
                        .eq(Teachplan::getGrade, grade)
                        .gt(Teachplan::getOrderby, orderby)
                        .orderByAsc(Teachplan::getOrderby)
                        .last("LIMIT 1");
                Teachplan nextGradeOne = teachplanMapper.selectOne(queryWrapper);
                exchangeOrderby(teachplan, nextGradeOne);
            } else if (2 == grade) { // 小章节
                // 小章节下移动
                // SELECT * FROM teachplan
                // WHERE parentId = #{parentId}
                // AND grade = 2
                // AND orderby > #{orderby}
                // ORDER BY orderby ASC
                // LIMIT 1
                LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Teachplan::getParentid, parentid)
                        .eq(Teachplan::getGrade, grade)
                        .gt(Teachplan::getOrderby, orderby)
                        .orderByDesc(Teachplan::getOrderby)
                        .last("LIMIT 1");
                Teachplan nextGradeTwo = teachplanMapper.selectOne(queryWrapper);
                exchangeOrderby(teachplan, nextGradeTwo);
            }
        }

    }

    private void exchangeOrderby(Teachplan teachplan1, Teachplan teachplan2) {
        if (teachplan2 == null) {
            BusinessException.cast("已经到头了，无法再移动辣");
        } else {
            Integer orderby1 = teachplan1.getOrderby();
            Integer orderby2 = teachplan2.getOrderby();
            teachplan1.setOrderby(orderby2);
            teachplan2.setOrderby(orderby1);

            teachplanMapper.updateById(teachplan1);
            teachplanMapper.updateById(teachplan2);
        }
    }

    @Override
    @Transactional
    public void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {

        // 教学计划id
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan == null) {
            BusinessException.cast("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if (grade != 2) {
            BusinessException.cast("只允许第二级教学计划绑定媒资文件");
        }
        // 课程id
        Long courseId = teachplan.getCourseId();


        // 根据课程计划Id删除原先绑定的媒资信息
        LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachplanMedia::getTeachplanId, teachplanId);
        teachplanMediaMapper.delete(queryWrapper);

        // 再添加教学计划与媒资的绑定关系
        TeachplanMedia teachplanMedia = new TeachplanMedia();

        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);
    }

    @Override
    public void unassociationMedia(Long teachPlanId, Long mediaId) {
        LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(TeachplanMedia::getTeachplanId, teachPlanId)
                .eq(TeachplanMedia::getMediaId, mediaId);
        teachplanMediaMapper.delete(queryWrapper);
    }
}