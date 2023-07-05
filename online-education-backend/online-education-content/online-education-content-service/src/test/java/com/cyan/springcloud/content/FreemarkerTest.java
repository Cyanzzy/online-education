package com.cyan.springcloud.content;

import com.cyan.springcloud.content.service.CoursePublishService;
import com.cyan.springcloud.model.dto.CoursePreviewDto;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 页面静态化测试
 *
 * @author Cyan Chau
 * @create 2023-07-05
 */
@SpringBootTest
public class FreemarkerTest {

    @Resource
    private CoursePublishService coursePublishService;

    @Test
    public void testGenerateHtmlByTemplate() throws IOException, TemplateException {

        Configuration configuration = new Configuration(Configuration.getVersion());

        // classpath路径
        String classpath = this.getClass().getResource("/").getPath();
        // 模板目录
        configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
        // 编码
        configuration.setDefaultEncoding("utf-8");
        // 模板
        Template template = configuration.getTemplate("course_template.ftl");
        // 数据
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(1L);

        Map<String, Object> model = new HashMap<>();
        model.put("model", coursePreviewInfo);

        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        InputStream inputStream = IOUtils.toInputStream(html, "utf-8");
        FileOutputStream outputStream = new FileOutputStream(new File("D:\\data\\upload\\1.html"));
        IOUtils.copy(inputStream, outputStream);

    }
}
