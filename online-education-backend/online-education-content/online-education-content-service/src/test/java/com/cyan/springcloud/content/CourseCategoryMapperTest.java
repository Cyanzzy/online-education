package com.cyan.springcloud.content;

import com.cyan.springcloud.content.mapper.CourseCategoryMapper;
import com.cyan.springcloud.model.dto.CourseCategoryTreeDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * CourseCategoryMapperTest
 *
 * @author Cyan Chau
 * @create 2023-02-10
 */
@SpringBootTest
public class CourseCategoryMapperTest {

    @Resource
    private CourseCategoryMapper courseCategoryMapper;

    @Test
    public void testCourseCategoryMapper() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }
}
