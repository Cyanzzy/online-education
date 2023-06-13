package com.cyan.springcloud.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyan.springcloud.base.exception.BusinessException;
import com.cyan.springcloud.content.mapper.CourseTeacherMapper;
import com.cyan.springcloud.content.service.CourseTeacherService;
import com.cyan.springcloud.model.dto.CourseTeacherDto;
import com.cyan.springcloud.model.po.CourseTeacher;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 师资管理接口实现
 *
 * @author Cyan Chau
 * @create 2023-06-13
 */
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Resource
    private CourseTeacherMapper courseTeacherMapper;

    @Override
    public List<CourseTeacher> getCourseTeacherList(Long courseId) {
        // SELECT * FROM course_teacher WHERE courseId = #{courseId}
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);

        return courseTeacherMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public CourseTeacher saveCourseTeacher(CourseTeacherDto courseTeacherDto) {
        // 根据id的存在性判断调用哪种功能
        Long id = courseTeacherDto.getId();
        
        if (id == null) { // id不存在，表示新增教师功能
            CourseTeacher courseTeacher = new CourseTeacher();
            BeanUtils.copyProperties(courseTeacherDto, courseTeacher);
            courseTeacher.setCreateDate(LocalDateTime.now());
            int result = courseTeacherMapper.insert(courseTeacher);
            if (result <= 0) {
                BusinessException.cast("新增教师失败");
            }
            return getCourseTeacher(courseTeacher);
        } else { // id存在，表示修改教师功能
            CourseTeacher courseTeacher = courseTeacherMapper.selectById(id);
            BeanUtils.copyProperties(courseTeacherDto, courseTeacher);
            int result = courseTeacherMapper.updateById(courseTeacher);
            if (result <= 0) {
                BusinessException.cast("修改教师失败");
            }
            return getCourseTeacher(courseTeacher);
        }
    }

    public CourseTeacher getCourseTeacher(CourseTeacher courseTeacher) {
        return courseTeacherMapper.selectById(courseTeacher.getId());
    }

    @Override
    public void deleteCourseTeacher(Long courseId, Long teacherId) {
        // DELETE course_teacher WHERE courseId = #{courseId} AND teacherId = #{teacher}
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId).eq(CourseTeacher::getId, teacherId);
        int result = courseTeacherMapper.delete(queryWrapper);
        if (result < 0) {
            BusinessException.cast("删除教师失败");
        }
    }
}
