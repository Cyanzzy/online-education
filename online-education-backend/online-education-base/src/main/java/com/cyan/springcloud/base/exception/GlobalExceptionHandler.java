package com.cyan.springcloud.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局异常处理器
 * @author Cyan Chau
 * @create 2023-06-07
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 自定义异常
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse doBusinessException(BusinessException e) {
        // 记录异常
        log.error("捕获异常：{}", e.getErrMessage());
        e.printStackTrace();

        // 解析异常信息
        String errMessage = e.getErrMessage();
        return new RestErrorResponse(errMessage);
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse doException(Exception e) {
        // 记录异常
        log.error("捕获异常：{}", e.getMessage());
        e.printStackTrace();

        // 解析异常信息
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse doMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        BindingResult bindingResult = e.getBindingResult();
        // 校验的错误信息
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        StringBuffer errors = new StringBuffer();
        fieldErrors.forEach(error->{
            errors.append(error.getDefaultMessage()).append(",");
        });

        // 解析异常信息
        return new RestErrorResponse(errors.toString());
    }

}
