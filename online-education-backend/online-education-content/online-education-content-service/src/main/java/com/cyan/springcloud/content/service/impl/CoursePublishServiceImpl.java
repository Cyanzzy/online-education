package com.cyan.springcloud.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.cyan.springcloud.base.exception.BusinessException;
import com.cyan.springcloud.base.exception.CommonError;
import com.cyan.springcloud.content.config.MultipartSupportConfig;
import com.cyan.springcloud.content.feignclient.MediaServiceClient;
import com.cyan.springcloud.content.mapper.CourseBaseMapper;
import com.cyan.springcloud.content.mapper.CourseMarketMapper;
import com.cyan.springcloud.content.mapper.CoursePublishMapper;
import com.cyan.springcloud.content.mapper.CoursePublishPreMapper;
import com.cyan.springcloud.content.service.CourseBaseInfoService;
import com.cyan.springcloud.content.service.CoursePublishService;
import com.cyan.springcloud.content.service.TeachplanService;
import com.cyan.springcloud.messagesdk.model.po.MqMessage;
import com.cyan.springcloud.messagesdk.service.MqMessageService;
import com.cyan.springcloud.model.dto.CourseBaseInfoDto;
import com.cyan.springcloud.model.dto.CoursePreviewDto;
import com.cyan.springcloud.model.dto.TeachplanDto;
import com.cyan.springcloud.model.po.CourseBase;
import com.cyan.springcloud.model.po.CourseMarket;
import com.cyan.springcloud.model.po.CoursePublish;
import com.cyan.springcloud.model.po.CoursePublishPre;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程预览逻辑接口实现
 *
 * @author Cyan Chau
 * @create 2023-07-04
 */
@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Resource
    private CourseBaseInfoService courseBaseInfoService;

    @Resource
    private TeachplanService teachplanService;

    @Resource
    private MqMessageService mqMessageService;

    @Resource
    private CourseBaseMapper courseBaseMapper;

    @Resource
    private CourseMarketMapper courseMarketMapper;

    @Resource
    private CoursePublishPreMapper coursePublishPreMapper;

    @Resource
    private CoursePublishMapper coursePublishMapper;

    @Resource
    private MediaServiceClient mediaServiceClient;


    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {

        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();

        // 课程基本信息 课程营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfo);

        // 课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        coursePreviewDto.setTeachplans(teachplanTree);

        return coursePreviewDto;
    }

    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {

        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        if (courseBaseInfo == null) {
            BusinessException.cast("课程未找到");
        }

        // 审核状态
        String auditStatus = courseBaseInfo.getAuditStatus();
        // 约束1 如果课程的审核状态未已条件则不允许提交
        if (auditStatus.equals("202003")) {
            BusinessException.cast("课程已提交，请等待审核");
        }
        // 约束2 课程图片没有则不允许提交
        String pic = courseBaseInfo.getPic();
        if (StringUtils.isEmpty(pic)) {
            BusinessException.cast("请上传图片");
        }
        // 约束3 课程计划没有则不允许提交
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if (teachplanTree == null || teachplanTree.size() == 0) {
            BusinessException.cast("请填写课程计划");
        }
        // 约束4 本机构只能提交本机构的课程
        if (!courseBaseInfo.getCompanyId().equals(companyId)) {
            BusinessException.cast("不允许提交其他机构的课程");
        }
        // 将课程基本信息、课程营销信息、课程计划信息插入课程发布表
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);

        // 设置机构id
        coursePublishPre.setCompanyId(companyId);

        // 课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        // 课程营销信息Java对象--> Json格式
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);

        // 课程计划信息
        String teachplanTreeJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeJson);

        // 预发布状态-->已提交
        coursePublishPre.setStatus("202003");

        // 提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());

        // 查询课程发布表，如果有记录则执行更新，没有则插入
        CoursePublishPre coursePublishPreObj = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreObj == null) {
            // 执行插入
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            // 执行更新
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        // 更新课程基本信息表的审核状态为已提交
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003"); // 审核状态 已提交

        courseBaseMapper.updateById(courseBase);
    }

    @Override
    public void publish(Long companyId, Long courseId) {

        // 查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            BusinessException.cast("该课程没有审核记录，无法发布");
        }

        // 查询审核状态
        String status = coursePublishPre.getStatus();
        // 如果课程审核未通过，则不予通过
        if (!status.equals("202004")) {
            BusinessException.cast("该课程没有审核通过，无法发布");
        }

        // 向课程发布表写入数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);

        // 查询课程发布表。如果记录存在则进行更新，如果记录不存在，则进行添加
        CoursePublish coursePublishObj = coursePublishMapper.selectById(courseId);
        if (coursePublishObj == null) {
            coursePublishMapper.insert(coursePublish);
        } else {
            coursePublishMapper.updateById(coursePublish);
        }

        // 向消息表写入数据
        saveCoursePublishMessage(courseId);

        // 将课程预发布表的记录删除
        coursePublishPreMapper.deleteById(courseId);
    }

    /**
     * 保存消息表记录
     *
     * @param courseId 课程id
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);

        if (mqMessage == null) {
            BusinessException.cast(CommonError.UNKOWN_ERROR);
        }
    }

    @Override
    public File generateCourseHtml(Long courseId) {

        File htmlFile = null;
        Configuration configuration = new Configuration(Configuration.getVersion());
        try {
            // classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            // 模板目录
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            // 编码
            configuration.setDefaultEncoding("utf-8");
            // 模板
            Template template = configuration.getTemplate("course_template.ftl");
            // 数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

            Map<String, Object> model = new HashMap<>();
            model.put("model", coursePreviewInfo);

            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            InputStream inputStream = IOUtils.toInputStream(html, "utf-8");
            htmlFile = File.createTempFile("coursepublish", ".html");
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("页面静态化出现问题，课程id：{}", courseId, e);
            e.printStackTrace();
        }

        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
       try {
           // file-->MultipartFile
           MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
           // 远程调用媒资服务
           String upload = mediaServiceClient.upload(multipartFile, "course/" + courseId + ".html");
           if (upload == null) {
               log.info("该服务已经降级处理，课程id：{}", courseId);
               BusinessException.cast("上传静态文件过程中出现异常");
           }
       } catch (Exception e) {
           e.printStackTrace();
           BusinessException.cast("上传静态文件过程中出现异常");
       }
    }
}
