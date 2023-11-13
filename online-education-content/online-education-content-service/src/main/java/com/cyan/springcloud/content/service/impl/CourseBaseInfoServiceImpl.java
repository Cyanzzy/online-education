package com.cyan.springcloud.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.springcloud.base.exception.BusinessException;
import com.cyan.springcloud.base.model.PageParams;
import com.cyan.springcloud.base.model.PageResult;
import com.cyan.springcloud.content.mapper.*;
import com.cyan.springcloud.content.service.CourseBaseInfoService;
import com.cyan.springcloud.content.service.CourseMarketService;
import com.cyan.springcloud.model.dto.AddCourseDto;
import com.cyan.springcloud.model.dto.CourseBaseInfoDto;
import com.cyan.springcloud.model.dto.EditCourseDto;
import com.cyan.springcloud.model.dto.QueryCourseParamsDto;
import com.cyan.springcloud.model.po.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Cyan Chau
 * @create 2023-01-25
 */
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Resource
    private CourseBaseMapper courseBaseMapper;

    @Resource
    private CourseMarketMapper courseMarketMapper;

    @Resource
    private CourseCategoryMapper courseCategoryMapper;

    @Resource
    private CourseMarketServiceImpl courseMarketService;

    @Resource
    private CourseTeacherMapper courseTeacherMapper;

    @Resource
    private TeachplanMapper teachplanMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {

        // 查询条件构造器
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        // 拼接查询条件
        // 根据课程名称查询 name like %名称%
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName());

        // 根据课程审核状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());

        // 根据课程发布状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());

        // 根据培训机构
        queryWrapper.eq(CourseBase::getCompanyId, companyId);

        // 分页参数
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        // 分页查询
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        // 数据
        List<CourseBase> items = pageResult.getRecords();
        // 总记录数
        long total = pageResult.getTotal();

        // 准备返回数据
        PageResult<CourseBase> result = new PageResult<>(items, total, pageParams.getPageNo(), pageParams.getPageSize());

        return result;
    }

    @Override
    @Transactional
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
//        // 参数进行合法性校验
//        if (StringUtils.isBlank(dto.getName())) {
//            throw new BusinessException("课程名称为空");
//        }
//
//        if (StringUtils.isBlank(dto.getMt())) {
//            throw new BusinessException("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(dto.getSt())) {
//            throw new BusinessException("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(dto.getGrade())) {
//            throw new BusinessException("课程等级为空");
//        }
//
//        if (StringUtils.isBlank(dto.getTeachmode())) {
//            throw new BusinessException("教育模式为空");
//        }
//
//        if (StringUtils.isBlank(dto.getUsers())) {
//            throw new BusinessException("适应人群为空");
//        }
//
//        if (StringUtils.isBlank(dto.getCharge())) {
//            throw new BusinessException("收费规则为空");
//        }

        // 课程基本信息对象
        CourseBase courseBase = new CourseBase();
        // 将dto中属性名一样的属性值拷贝到courseBase
        BeanUtils.copyProperties(dto, courseBase);
        // 设置机构id
        courseBase.setCompanyId(companyId);
        // 设置创建时间
        courseBase.setCreateDate(LocalDateTime.now());
        // 设置审核状态，默认为未提交
        courseBase.setAuditStatus("202002");
        // 设置发布状态，默认为未发布
        courseBase.setStatus("203001");
        // 课程基本表插入记录
        int insertcourseBase = courseBaseMapper.insert(courseBase);
        // 获取课程id
        Long courseId = courseBase.getId();

        // 课程营销对象
        CourseMarket courseMarket = new CourseMarket();
        // 将dto中属性名一样的属性值拷贝到courseMarket
        BeanUtils.copyProperties(dto, courseMarket);
        courseMarket.setId(courseId);
        // 校验如果课程收费，价格必须输入
        String charge = dto.getCharge();
        if (charge.equals("201001")) { // 201001表示收费
            if (courseMarket.getPrice() == null || courseMarket.getPrice().floatValue() <= 0) {
//                throw new RuntimeException("付费课程，但价格空");
                BusinessException.cast("付费课程，但价格空");
            }
        }
        // 课程营销表插入记录
        int insertCourseMarket = this.saveCourseMarket(courseMarket);

        // 判断插入是否成功
        if (insertcourseBase <= 0 || insertCourseMarket <= 0) {
            throw new BusinessException("课程添加失败");
        }

        // 组装返回结果
        return getCourseBaseInfo(courseId);
    }

    /**
     * 根据课程id查询课程的基本信息和营销信息
     *
     * @param courseId
     * @return
     */
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {

        // 基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);

        // 营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        // 组装信息
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }

        // 根据课程分类的id
        String mt = courseBase.getMt();
        String st = courseBase.getSt();

        CourseCategory mtCategory = courseCategoryMapper.selectById(mt);
        CourseCategory stCategory = courseCategoryMapper.selectById(st);
        if (mtCategory != null) {
            // 大分类名称
            String mtName = mtCategory.getName();
            courseBaseInfoDto.setMtName(mtName);
        }
        if (stCategory != null) {
            // 小分类名称
            String stName = stCategory.getName();
            courseBaseInfoDto.setStName(stName);
        }

        return courseBaseInfoDto;
    }

    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {

        // 获取课程Id
        Long id = editCourseDto.getId();
        // 查询课程信息
        CourseBase courseBase = courseBaseMapper.selectById(id);
        if (courseBase == null) {
            BusinessException.cast("课程不存在");
        }

        // 数据合法性校验。根据具体的业务逻辑去校验，本机构只能修改自己的课程
        if (!companyId.equals(courseBase.getCompanyId())) {
            BusinessException.cast("本机构只能修改自己的课程");
        }

        // 封装数据
        BeanUtils.copyProperties(editCourseDto, courseBase);
        // 修改时间
        courseBase.setChangeDate(LocalDateTime.now());
        // 更新课程基本信息
        int result = courseBaseMapper.updateById(courseBase);
        if (result <= 0) {
            BusinessException.cast("修改课程失败");
        }

        // 封装课程营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto, courseMarket);

        saveCourseMarket(courseMarket);

        // 查询课程信息
        return this.getCourseBaseInfo(id);
    }

    // 保存营销信息
    private int saveCourseMarket(CourseMarket courseMarket) {
        String charge = courseMarket.getCharge();
        if (StringUtils.isBlank(charge)) {
            BusinessException.cast("收费规则未选择");
        }
        if (charge.equals("201001")) {
            if (courseMarket.getPrice() == null || courseMarket.getPrice().floatValue() <= 0) {
                BusinessException.cast("课程为收费课，价格不能为空且必须大于0");
            }
        }
        boolean result = courseMarketService.saveOrUpdate(courseMarket);
        return result ? 1 : 0;
    }

    @Override
    @Transactional
    public void deleteCourse(Long companyId, Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (!companyId.equals(courseBase.getCompanyId())) {
            BusinessException.cast("只允许删除本机构的课程");
        }
        // 删除课程教师信息
        LambdaQueryWrapper<CourseTeacher> teacherLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teacherLambdaQueryWrapper.eq(CourseTeacher::getCourseId, courseId);
        courseTeacherMapper.delete(teacherLambdaQueryWrapper);

        // 删除课程计划
        LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanLambdaQueryWrapper.eq(Teachplan::getCourseId, courseId);
        teachplanMapper.delete(teachplanLambdaQueryWrapper);

        // 删除营销信息
        courseMarketMapper.deleteById(courseId);

        // 删除课程基本信息
        courseBaseMapper.deleteById(courseId);
    }
}
