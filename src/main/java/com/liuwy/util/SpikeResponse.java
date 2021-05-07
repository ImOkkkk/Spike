package com.liuwy.util;


import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

/**
 * @author:
 * @date: created in 20:49 2021/4/15
 * @version:
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpikeResponse<T>{

    private static final long serialVersionUID = -4769652794035776943L;

    private boolean success;

    private String msg;

    private T data;

    public SpikeResponse() {
    }

    public SpikeResponse(boolean success) {
        this.success = success;
    }

    public SpikeResponse(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public SpikeResponse(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public static <T> SpikeResponse<T> success() {
        return new SpikeResponse<>(true);
    }

    public static <T> SpikeResponse<T> successWithMsg(String msg) {
        return new SpikeResponse<>(true, msg);
    }

    public static <T> SpikeResponse<T> successWithData(T data) {
        return new SpikeResponse<>(true, data);
    }

    public static <T> SpikeResponse<T> fail() {
        return new SpikeResponse<>(false);
    }

    public static <T> SpikeResponse<T> failWithMsg(String msg) {
        return new SpikeResponse<>(false, msg);
    }
}