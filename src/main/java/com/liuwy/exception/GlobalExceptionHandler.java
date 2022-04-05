package com.liuwy.exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.liuwy.util.SpikeResponse;

/**
 * 全局异常处理
 *
 * @author ImOkkkk
 * @date 2022/4/4 16:12
 * @since 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public SpikeResponse doError(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        SpikeResponse spikeResponse = new SpikeResponse();
        if (ex instanceof NullPointerException) {
            spikeResponse.setMsg("某必要参数为空！");
            spikeResponse.setSuccess(false);
        } else {
            spikeResponse.setMsg(ex.getLocalizedMessage());
            spikeResponse.setSuccess(false);
        }
        LOGGER.error(request.getRequestURI() + "_" + ex.getMessage(), ex);
        return spikeResponse;
    }
}
