package com.cyan.springcloud.base.exception;

import lombok.Data;

/**
 * 自定义业务异常
 *
 * @author Cyan Chau
 * @create 2023-06-07
 */
public class BusinessException extends RuntimeException {

    private String errMessage;

    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
        this.errMessage = message;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public static void cast(CommonError commonError){
        throw new BusinessException(commonError.getErrMessage());
    }

    public static void cast(String errMessage){
        throw new BusinessException(errMessage);
    }
}
