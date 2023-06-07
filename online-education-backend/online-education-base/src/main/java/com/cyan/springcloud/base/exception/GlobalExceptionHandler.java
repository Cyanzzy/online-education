package com.cyan.springcloud.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    public RestErrorResponse customException(BusinessException e) {
        // 记录异常
        log.error("系统异常{}", e.getErrMessage(), e);

        // 解析异常信息
        String errMessage = e.getErrMessage();
        return new RestErrorResponse(errMessage);
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse exception(BusinessException e) {
        // 记录异常
        log.error("系统异常{}", e.getErrMessage(), e);

        // 解析异常信息
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }
}
