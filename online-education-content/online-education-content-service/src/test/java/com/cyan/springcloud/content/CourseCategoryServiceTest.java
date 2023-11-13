package com.cyan.springcloud.content;

import com.cyan.springcloud.content.service.CourseCategoryService;
import com.cyan.springcloud.model.dto.CourseCategoryTreeDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * CourseCategoryServiceTest
 *
 * @author Cyan Chau
 * @create 2023-06-07
 */
@SpringBootTest
public class CourseCategoryServiceTest {

    @Autowired
    private CourseCategoryService courseCategoryService;

    @Test
    public void testQueryTreeNodes() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.queryTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }
}
