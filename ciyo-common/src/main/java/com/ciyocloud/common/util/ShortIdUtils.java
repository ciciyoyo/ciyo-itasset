package com.ciyocloud.common.util;

import cn.hutool.core.lang.id.NanoId;
import lombok.experimental.UtilityClass;

/**
 * 短Id工具类
 */
@UtilityClass
public class ShortIdUtils {

    /**
     * 默认随机字母表，使用URL安全的Base64字符
     */
    private static final char[] DEFAULT_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();


    /**
     * 生成8位长度Id
     *
     * @return
     */
    public String genId() {
        return NanoId.randomNanoId(null, DEFAULT_ALPHABET, 8);
    }

    /**
     * 生成8位长度Id
     *
     * @return
     */
    public String genId(int length) {
        return NanoId.randomNanoId(null, DEFAULT_ALPHABET, length);
    }
}
