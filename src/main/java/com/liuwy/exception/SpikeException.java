package com.liuwy.exception;

import lombok.Data;

/**
 * @author:
 * @date: created in 9:32 2021/4/16
 * @version:
 */
@Data
public class SpikeException extends RuntimeException {
    private static final long serialVersionUID = -4341289870902198315L;

    private String logMsg;

    private String[] args;

    public SpikeException() {
        super();
    }

    public SpikeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SpikeException(String message, String logMsg) {
        super(message);
        this.logMsg = logMsg;
    }

    public SpikeException(String message, String... args) {
        super(message);
        this.args = args;

    }

    public SpikeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpikeException(String message) {
        super(message);
    }


    public SpikeException(Throwable cause) {
        super(cause);
    }
}