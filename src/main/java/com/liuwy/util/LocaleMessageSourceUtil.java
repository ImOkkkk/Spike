package com.liuwy.util;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * @author:
 * @date: created in 9:40 2021/4/16
 * @version:
 */
public class LocaleMessageSourceUtil {
    public static MessageSource messageSource;

    public LocaleMessageSourceUtil(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public static String getMessage(String code) {
        return getMessage(code, (Object[])null);
    }

    public static String getMessage(String code, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, locale);
    }

    public static String getMessage(String code, Object[] args, String defaultMessage) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }

    public static String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }
}