package com.yax.redisqueue.messageModel;

import com.alibaba.fastjson.JSONObject;
import org.springframework.validation.BindingResult;

import java.io.Serializable;

/**
 *  @author yax
 *
 */
@SuppressWarnings("all")
public class ResponseModel<T> implements Serializable{

    private int code;// 0成功 其他失败

    private String msg;

    private T data;

    public ResponseModel() {
    }

    public ResponseModel(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = (T) data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static ResponseModel<?> success(Object object) {
        return new ResponseModel(0, null, object);
    }
    public static ResponseModel<?> success() {
        return success(null);
    }
    public static ResponseModel<?> success(String msg,Object object) {
        return new ResponseModel(0, msg, object);
    }
    public static ResponseModel<?> error(int code, String msg) {
        return new ResponseModel(code, msg, null);
    }

    public static ResponseModel<?> error(String msg) {
        return error(2, msg);
    }

    public static ResponseModel<?> error(BindingResult bindingResult) {
        return error(2, bindingResult.getFieldError().getDefaultMessage());
    }

    public static ResponseModel<?> fail(String msg) {
        return new ResponseModel(1, msg, null);
    }
    public static ResponseModel<?> fail(String msg,Object data) {
        return new ResponseModel(1, msg, data);
    }

    public static ResponseModel<?> fail() {
        return new ResponseModel(1, null, null);
    }

    public static ResponseModel<?> instantiation(int code, String msg, Object data) {
        return new ResponseModel(code,msg ,data);
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
