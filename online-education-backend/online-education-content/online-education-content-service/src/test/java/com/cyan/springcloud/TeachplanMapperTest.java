package com.cyan.springcloud;

import com.cyan.springcloud.content.mapper.TeachplanMapper;
import com.cyan.springcloud.model.dto.TeachplanDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Cyan Chau
 * @create 2023-06-11
 */
@SpringBootTest
public class TeachplanMapperTest {

    @Resource
    private TeachplanMapper teachplanMapper;

    @Test
    public void testSelectTreeNodes() {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(117L);
        System.out.println(teachplanDtos);

    }
}
