package com.cyan.springcloud.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 全局异常处理器
 * @author Cyan Chau
 * @create 2023-06-07
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // 自定义异常
    @ResponseBody
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse doBusinessException(BusinessException e) {
        // 记录异常
        log.error("捕获异常：{}", e.getErrMessage());
        e.printStackTrace();

        // 解析异常信息
        return new RestErrorResponse(e.getErrMessage());
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse doException(Exception e) {
        // 记录异常
        log.error("捕获异常：{}", e.getMessage());
        e.printStackTrace();

        // 解析异常信息
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse doMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        BindingResult bindingResult = e.getBindingResult();
        List<String> errors = new ArrayList<>();
        bindingResult.getFieldErrors().stream().forEach(item->{
            errors.add(item.getDefaultMessage());
        });
        String errMessage = StringUtils.join(errors, ",");
        log.error("捕获异常：{}", e.getMessage(), errMessage);
        return new RestErrorResponse(errMessage);
    }

}
