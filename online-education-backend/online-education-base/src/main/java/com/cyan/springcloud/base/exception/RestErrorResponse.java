package com.cyan.springcloud.base.exception;

import java.io.Serializable;

/**
 * 错误响应参数包装
 *
 * @author Cyan Chau
 * @create 2023-06-07
 */
public class RestErrorResponse implements Serializable {

    private String errMessage;

    public RestErrorResponse(String errMessage){
        this.errMessage= errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
}