package com.cyan.springcloud.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyan.springcloud.base.exception.BusinessException;
import com.cyan.springcloud.base.model.PageResult;
import com.cyan.springcloud.learning.feignclient.ContentServiceClient;
import com.cyan.springcloud.learning.mapper.OlChooseCourseMapper;
import com.cyan.springcloud.learning.mapper.OlCourseTablesMapper;
import com.cyan.springcloud.learning.model.dto.MyCourseTableParams;
import com.cyan.springcloud.learning.model.dto.OlChooseCourseDto;
import com.cyan.springcloud.learning.model.dto.OlCourseTablesDto;
import com.cyan.springcloud.learning.model.po.OlChooseCourse;
import com.cyan.springcloud.learning.model.po.OlCourseTables;
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
    private OlChooseCourseMapper olChooseCourseMapper;

    @Resource
    private OlCourseTablesMapper olCourseTablesMapper;

    @Resource
    private ContentServiceClient contentServiceClient;

    @Resource
    private OlCourseTablesMapper courseTablesMapper;

    @Override
    @Transactional
    public OlChooseCourseDto addChooseCourse(String userId, Long courseId) {

        // 选课调用内容管理查询课程的收费规则
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);

        if (coursepublish == null) {
            BusinessException.cast("课程不存在");
        }
        // 选课记录
        OlChooseCourse olChooseCourse = null;
        // 收费规则
        String charge = coursepublish.getCharge();
        if ("201000".equals(charge)) { // 课程免费
            // 如果课程免费，向选课记录表、我的课程表写数据
            olChooseCourse = addFreeCourse(userId, coursepublish); // 选课记录表
            OlCourseTables olCourseTables = addCourseTables(olChooseCourse); //我的课程表
        } else {
            // 如果课程收费，向选课记录表写数据
            olChooseCourse = addChargeCourse(userId, coursepublish); // 选课记录表
        }

        // 判断学生的学习资格
        OlCourseTablesDto xcCourseTablesDto = getLearningStatus(userId, courseId);

        // 构造返回值
        OlChooseCourseDto xcChooseCourseDto = new OlChooseCourseDto();
        // 拷贝属性
        BeanUtils.copyProperties(olChooseCourse, xcChooseCourseDto);
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
    public OlCourseTablesDto getLearningStatus(String userId, Long courseId) {

        OlCourseTablesDto xcCourseTablesDto = new OlCourseTablesDto();

        // 查询我的课程表，如果没有记录，说明没有选课
        OlCourseTables olCourseTables = getXcCourseTables(userId, courseId);
        if (olCourseTables == null) {
            // {"code":"702002","desc":"没有选课或选课后没有支付"},
            xcCourseTablesDto.setLearnStatus("702002");

            return xcCourseTablesDto;
        }

        // 如果查询到记录，判断是否过期，如果过期不能继续学习；如果未过期，可以继续学习
        boolean isExpires = olCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        if (isExpires) {
            // {"code":"702003","desc":"已过期需要申请续期或重新支付"}
            BeanUtils.copyProperties(olCourseTables, xcCourseTablesDto);
            xcCourseTablesDto.setLearnStatus("702003");

            return xcCourseTablesDto;
        } else {
            // {"code":"702001","desc":"正常学习"},
            BeanUtils.copyProperties(olCourseTables, xcCourseTablesDto);
            xcCourseTablesDto.setLearnStatus("70200");

            return xcCourseTablesDto;
        }
    }

    @Override
    @Transactional
    public boolean saveChooseCourseStatus(String chooseCourseId) {
        // 1. 根据选课id，查询选课表
        OlChooseCourse chooseCourse = olChooseCourseMapper.selectById(chooseCourseId);
        if (chooseCourse == null) {
            log.error("接收到购买课程的消息，根据选课id未查询到课程，选课id：{}", chooseCourseId);
            return false;
        }
        // 2. 选课状态为未支付时，更新选课状态为选课成功
        if ("701002".equals(chooseCourse.getStatus())) {
            chooseCourse.setStatus("701001");
            int update = olChooseCourseMapper.updateById(chooseCourse);
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
    public PageResult<OlCourseTables> mycourestabls(MyCourseTableParams params) {
        // 页码
        long pageNo = params.getPage();
        // 每页记录数,固定为4
        long pageSize = 4;
        // 分页条件
        Page<OlCourseTables> page = new Page<>(pageNo, pageSize);
        //根据用户id查询
        String userId = params.getUserId();
        LambdaQueryWrapper<OlCourseTables> lambdaQueryWrapper = new LambdaQueryWrapper<OlCourseTables>().eq(OlCourseTables::getUserId, userId);

        //分页查询
        Page<OlCourseTables> pageResult = courseTablesMapper.selectPage(page, lambdaQueryWrapper);
        List<OlCourseTables> records = pageResult.getRecords();
        //记录总数
        long total = pageResult.getTotal();
        PageResult<OlCourseTables> courseTablesResult = new PageResult<>(records, total, pageNo, pageSize);
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
    private OlChooseCourse addFreeCourse(String userId, CoursePublish coursePublish) {

        // 课程id
        Long courseId = coursePublish.getId();
        // 如果存在免费的选课记录 && 选课状态==成功 --> 返回
        LambdaQueryWrapper<OlChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(OlChooseCourse::getUserId, userId)
                .eq(OlChooseCourse::getCourseId, courseId)
                .eq(OlChooseCourse::getOrderType, "700001") // 免费课程
                .eq(OlChooseCourse::getStatus, "701001"); // 选课成功
        List<OlChooseCourse> list = olChooseCourseMapper.selectList(queryWrapper);

        if (list.size() > 0) {
            return list.get(0);
        }

        // 向选课记录表写数据
        OlChooseCourse olChooseCourse = new OlChooseCourse();

        olChooseCourse.setCourseId(courseId);
        olChooseCourse.setCourseName(coursePublish.getName());
        olChooseCourse.setUserId(userId);
        olChooseCourse.setCompanyId(coursePublish.getCompanyId());
        olChooseCourse.setOrderType("700001"); // 免费课程
        olChooseCourse.setCreateDate(LocalDateTime.now());
        olChooseCourse.setCoursePrice(coursePublish.getPrice());
        olChooseCourse.setStatus("701001"); // 选课成功
        olChooseCourse.setValidDays(365); // 课程有效期
        olChooseCourse.setValidtimeStart(LocalDateTime.now()); // 有效期开始时间
        olChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365)); // 有效期结束时间

        int insert = olChooseCourseMapper.insert(olChooseCourse);

        if (insert <= 0) {
            BusinessException.cast("添加选课记录失败");
        }

        return olChooseCourse;
    }

    /**
     * 添加记录至我的课程表
     *
     * @param chooseCourse
     * @return
     */
    private OlCourseTables addCourseTables(OlChooseCourse chooseCourse) {

        // 选课成功，才能向我的课程表添加
        String status = chooseCourse.getStatus();
        if (!"701001".equals(status)) {
            BusinessException.cast("选课未成功，无法添加记录到课程表");
        }
        OlCourseTables olCourseTables = getXcCourseTables(chooseCourse.getUserId(), chooseCourse.getCourseId());
        if (olCourseTables != null) {
            return olCourseTables;
        }
        olCourseTables = new OlCourseTables();
        BeanUtils.copyProperties(chooseCourse, olCourseTables);

        olCourseTables.setChooseCourseId(chooseCourse.getId()); // 记录选课表的id
        olCourseTables.setCourseType(chooseCourse.getOrderType()); // 选课类型
        olCourseTables.setUpdateDate(LocalDateTime.now());

        int insert = olCourseTablesMapper.insert(olCourseTables);
        if (insert <= 0) {
            BusinessException.cast("添加我的课程表失败");
        }

        return olCourseTables;
    }

    /**
     * 添加收费课程
     *
     * @param userId
     * @param coursePublish
     * @return
     */
    private OlChooseCourse addChargeCourse(String userId, CoursePublish coursePublish) {
        // 课程id
        Long courseId = coursePublish.getId();
        // 如果存在收费的选课记录 && 选课状态==待支付 --> 返回
        LambdaQueryWrapper<OlChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(OlChooseCourse::getUserId, userId)
                .eq(OlChooseCourse::getCourseId, courseId)
                .eq(OlChooseCourse::getOrderType, "700002") // 收费课程
                .eq(OlChooseCourse::getStatus, "701002"); // 待支付
        List<OlChooseCourse> list = olChooseCourseMapper.selectList(queryWrapper);

        if (list.size() > 0) {
            return list.get(0);
        }

        // 向选课记录表写数据
        OlChooseCourse olChooseCourse = new OlChooseCourse();

        olChooseCourse.setCourseId(courseId);
        olChooseCourse.setCourseName(coursePublish.getName());
        olChooseCourse.setUserId(userId);
        olChooseCourse.setCompanyId(coursePublish.getCompanyId());
        olChooseCourse.setOrderType("700002"); // 收费课程
        olChooseCourse.setCreateDate(LocalDateTime.now());
        olChooseCourse.setCoursePrice(coursePublish.getPrice());
        olChooseCourse.setStatus("701002"); // 待支付
        olChooseCourse.setValidDays(365); // 课程有效期
        olChooseCourse.setValidtimeStart(LocalDateTime.now()); // 有效期开始时间
        olChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365)); // 有效期结束时间

        int insert = olChooseCourseMapper.insert(olChooseCourse);

        if (insert <= 0) {
            BusinessException.cast("添加选课记录失败");
        }

        return olChooseCourse;
    }

    private OlCourseTables getXcCourseTables(String userId, Long courseId) {
        return olCourseTablesMapper.selectOne(
                new LambdaQueryWrapper<OlCourseTables>()
                        .eq(OlCourseTables::getUserId, userId)
                        .eq(OlCourseTables::getCourseId, courseId));
    }
}
