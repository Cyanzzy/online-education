package com.cyan.springcloud.content.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Freemarker测试程序
 *
 * @author Cyan Chau
 * @create 2023-07-03
 */
@Controller
public class FreemarkerController {

    @GetMapping("/testfreemarker")
    public ModelAndView test(){
        ModelAndView modelAndView = new ModelAndView();
        // 设置模型数据
        modelAndView.addObject("name","小洋");
        // 设置模板名称
        modelAndView.setViewName("test");
        return modelAndView;
    }

}
