package com.cyan.springcloud.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.springcloud.base.exception.BusinessException;
import com.cyan.springcloud.base.model.PageResult;
import com.cyan.springcloud.learning.feignclient.ContentServiceClient;
import com.cyan.springcloud.learning.mapper.XcChooseCourseMapper;
import com.cyan.springcloud.learning.mapper.XcCourseTablesMapper;
import com.cyan.springcloud.learning.model.dto.MyCourseTableParams;
import com.cyan.springcloud.learning.model.dto.XcChooseCourseDto;
import com.cyan.springcloud.learning.model.dto.XcCourseTablesDto;
import com.cyan.springcloud.learning.model.po.XcChooseCourse;
import com.cyan.springcloud.learning.model.po.XcCourseTables;
import com.cyan.springcloud.learning.service.MyCourseTablesService;
import com.cyan.springcloud.model.po.CoursePublish;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Cyan Chau
 * @create 2023-07-08
 */
@Slf4j
@Service
public class MyCourseTablesServiceImpl implements MyCourseTablesService {

    @Resource
    private XcChooseCourseMapper xcChooseCourseMapper;

    @Resource
    private XcCourseTablesMapper xcCourseTablesMapper;

    @Resource
    private ContentServiceClient contentServiceClient;

    @Resource
    private XcCourseTablesMapper courseTablesMapper;

    @Override
    @Transactional
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {

        // 选课调用内容管理查询课程的收费规则
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);

        if (coursepublish == null) {
            BusinessException.cast("课程不存在");
        }
        // 选课记录
        XcChooseCourse xcChooseCourse = null;
        // 收费规则
        String charge = coursepublish.getCharge();
        if ("201000".equals(charge)) { // 课程免费
            // 如果课程免费，向选课记录表、我的课程表写数据
            xcChooseCourse = addFreeCourse(userId, coursepublish); // 选课记录表
            XcCourseTables xcCourseTables = addCourseTables(xcChooseCourse); //我的课程表
        } else {
            // 如果课程收费，向选课记录表写数据
            xcChooseCourse = addChargeCourse(userId, coursepublish); // 选课记录表
        }

        // 判断学生的学习资格
        XcCourseTablesDto xcCourseTablesDto = getLearningStatus(userId, courseId);

        // 构造返回值
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        // 拷贝属性
        BeanUtils.copyProperties(xcChooseCourse, xcChooseCourseDto);
        // 设置学习资格状态
        xcChooseCourseDto.setLearnStatus(xcCourseTablesDto.getLearnStatus());

        return xcChooseCourseDto;
    }

    /*
     * [
     *  {"code":"702001","desc":"正常学习"},
     *  {"code":"702002","desc":"没有选课或选课后没有支付"},
     *  {"code":"702003","desc":"已过期需要申请续期或重新支付"}
     * ]
     */
    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {

        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();

        // 查询我的课程表，如果没有记录，说明没有选课
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        if (xcCourseTables == null) {
            // {"code":"702002","desc":"没有选课或选课后没有支付"},
            xcCourseTablesDto.setLearnStatus("702002");

            return xcCourseTablesDto;
        }

        // 如果查询到记录，判断是否过期，如果过期不能继续学习；如果未过期，可以继续学习
        boolean isExpires = xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        if (isExpires) {
            // {"code":"702003","desc":"已过期需要申请续期或重新支付"}
            BeanUtils.copyProperties(xcCourseTables, xcCourseTablesDto);
            xcCourseTablesDto.setLearnStatus("702003");

            return xcCourseTablesDto;
        } else {
            // {"code":"702001","desc":"正常学习"},
            BeanUtils.copyProperties(xcCourseTables, xcCourseTablesDto);
            xcCourseTablesDto.setLearnStatus("70200");

            return xcCourseTablesDto;
        }
    }

    @Override
    @Transactional
    public boolean saveChooseCourseStatus(String chooseCourseId) {
        // 1. 根据选课id，查询选课表
        XcChooseCourse chooseCourse = xcChooseCourseMapper.selectById(chooseCourseId);
        if (chooseCourse == null) {
            log.error("接收到购买课程的消息，根据选课id未查询到课程，选课id：{}", chooseCourseId);
            return false;
        }
        // 2. 选课状态为未支付时，更新选课状态为选课成功
        if ("701002".equals(chooseCourse.getStatus())) {
            chooseCourse.setStatus("701001");
            int update = xcChooseCourseMapper.updateById(chooseCourse);
            if (update <= 0) {
                log.error("更新选课记录失败：{}", chooseCourse);
                BusinessException.cast("更新选课记录失败");
            }
        }
        // 3. 向我的课程表添加记录
        addCourseTables(chooseCourse);
        return true;
    }

    @Override
    public PageResult<XcCourseTables> mycourestabls(MyCourseTableParams params) {
        // 页码
        long pageNo = params.getPage();
        // 每页记录数,固定为4
        long pageSize = 4;
        // 分页条件
        Page<XcCourseTables> page = new Page<>(pageNo, pageSize);
        //根据用户id查询
        String userId = params.getUserId();
        LambdaQueryWrapper<XcCourseTables> lambdaQueryWrapper = new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId);

        //分页查询
        Page<XcCourseTables> pageResult = courseTablesMapper.selectPage(page, lambdaQueryWrapper);
        List<XcCourseTables> records = pageResult.getRecords();
        //记录总数
        long total = pageResult.getTotal();
        PageResult<XcCourseTables> courseTablesResult = new PageResult<>(records, total, pageNo, pageSize);
        return courseTablesResult;

    }
    /**
     * 添加免费课程
     * 免费课程加入选课记录表、我的课程表
     *
     * @param userId
     * @param coursePublish
     * @return
     */
    private XcChooseCourse addFreeCourse(String userId, CoursePublish coursePublish) {

        // 课程id
        Long courseId = coursePublish.getId();
        // 如果存在免费的选课记录 && 选课状态==成功 --> 返回
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, courseId)
                .eq(XcChooseCourse::getOrderType, "700001") // 免费课程
                .eq(XcChooseCourse::getStatus, "701001"); // 选课成功
        List<XcChooseCourse> list = xcChooseCourseMapper.selectList(queryWrapper);

        if (list.size() > 0) {
            return list.get(0);
        }

        // 向选课记录表写数据
        XcChooseCourse xcChooseCourse = new XcChooseCourse();

        xcChooseCourse.setCourseId(courseId);
        xcChooseCourse.setCourseName(coursePublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursePublish.getCompanyId());
        xcChooseCourse.setOrderType("700001"); // 免费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setCoursePrice(coursePublish.getPrice());
        xcChooseCourse.setStatus("701001"); // 选课成功
        xcChooseCourse.setValidDays(365); // 课程有效期
        xcChooseCourse.setValidtimeStart(LocalDateTime.now()); // 有效期开始时间
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365)); // 有效期结束时间

        int insert = xcChooseCourseMapper.insert(xcChooseCourse);

        if (insert <= 0) {
            BusinessException.cast("添加选课记录失败");
        }

        return xcChooseCourse;
    }

    /**
     * 添加记录至我的课程表
     *
     * @param chooseCourse
     * @return
     */
    private XcCourseTables addCourseTables(XcChooseCourse chooseCourse) {

        // 选课成功，才能向我的课程表添加
        String status = chooseCourse.getStatus();
        if (!"701001".equals(status)) {
            BusinessException.cast("选课未成功，无法添加记录到课程表");
        }
        XcCourseTables xcCourseTables = getXcCourseTables(chooseCourse.getUserId(), chooseCourse.getCourseId());
        if (xcCourseTables != null) {
            return xcCourseTables;
        }
        xcCourseTables = new XcCourseTables();
        BeanUtils.copyProperties(chooseCourse, xcCourseTables);

        xcCourseTables.setChooseCourseId(chooseCourse.getId()); // 记录选课表的id
        xcCourseTables.setCourseType(chooseCourse.getOrderType()); // 选课类型
        xcCourseTables.setUpdateDate(LocalDateTime.now());

        int insert = xcCourseTablesMapper.insert(xcCourseTables);
        if (insert <= 0) {
            BusinessException.cast("添加我的课程表失败");
        }

        return xcCourseTables;
    }

    /**
     * 添加收费课程
     *
     * @param userId
     * @param coursePublish
     * @return
     */
    private XcChooseCourse addChargeCourse(String userId, CoursePublish coursePublish) {
        // 课程id
        Long courseId = coursePublish.getId();
        // 如果存在收费的选课记录 && 选课状态==待支付 --> 返回
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, courseId)
                .eq(XcChooseCourse::getOrderType, "700002") // 收费课程
                .eq(XcChooseCourse::getStatus, "701002"); // 待支付
        List<XcChooseCourse> list = xcChooseCourseMapper.selectList(queryWrapper);

        if (list.size() > 0) {
            return list.get(0);
        }

        // 向选课记录表写数据
        XcChooseCourse xcChooseCourse = new XcChooseCourse();

        xcChooseCourse.setCourseId(courseId);
        xcChooseCourse.setCourseName(coursePublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursePublish.getCompanyId());
        xcChooseCourse.setOrderType("700002"); // 收费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setCoursePrice(coursePublish.getPrice());
        xcChooseCourse.setStatus("701002"); // 待支付
        xcChooseCourse.setValidDays(365); // 课程有效期
        xcChooseCourse.setValidtimeStart(LocalDateTime.now()); // 有效期开始时间
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365)); // 有效期结束时间

        int insert = xcChooseCourseMapper.insert(xcChooseCourse);

        if (insert <= 0) {
            BusinessException.cast("添加选课记录失败");
        }

        return xcChooseCourse;
    }

    private XcCourseTables getXcCourseTables(String userId, Long courseId) {
        return xcCourseTablesMapper.selectOne(
                new LambdaQueryWrapper<XcCourseTables>()
                        .eq(XcCourseTables::getUserId, userId)
                        .eq(XcCourseTables::getCourseId, courseId));
    }
}
