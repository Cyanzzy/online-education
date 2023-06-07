package com.cyan.springcloud.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.springcloud.base.exception.BusinessException;
import com.cyan.springcloud.base.model.PageParams;
import com.cyan.springcloud.base.model.PageResult;
import com.cyan.springcloud.content.mapper.CourseBaseMapper;
import com.cyan.springcloud.content.mapper.CourseCategoryMapper;
import com.cyan.springcloud.content.mapper.CourseMarketMapper;
import com.cyan.springcloud.content.service.CourseBaseInfoService;
import com.cyan.springcloud.model.dto.AddCourseDto;
import com.cyan.springcloud.model.dto.CourseBaseInfoDto;
import com.cyan.springcloud.model.dto.QueryCourseParamsDto;
import com.cyan.springcloud.model.po.CourseBase;
import com.cyan.springcloud.model.po.CourseCategory;
import com.cyan.springcloud.model.po.CourseMarket;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
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

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {

        // 查询条件构造器
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        // 拼接查询条件
        // 根据课程名称查询 name like %名称%
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName());

        // 根据课程审核状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());

        // 根据课程发布状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());


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
        // 参数进行合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            throw new BusinessException("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            throw new BusinessException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            throw new BusinessException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            throw new BusinessException("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new BusinessException("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            throw new BusinessException("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            throw new BusinessException("收费规则为空");
        }

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
        int insertCourseMarket = courseMarketMapper.insert(courseMarket);

        // 判断插入是否成功
        if (insertcourseBase <= 0 || insertCourseMarket <= 0) {
            throw new RuntimeException("课程添加失败");
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
        BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);

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
            courseBaseInfoDto.setName(stName);
        }

        return courseBaseInfoDto;
    }
}
