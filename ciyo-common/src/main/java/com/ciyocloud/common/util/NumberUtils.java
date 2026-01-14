package com.ciyocloud.common.util;

import cn.hutool.core.util.NumberUtil;

/**
 * 数字工具类
 *
 * @author : codeck
 * @since :  2023/03/29 17:44
 **/
public class NumberUtils extends NumberUtil {

    /**
     * 判断数字不是Null或者0
     */
    public static boolean isNotNullOrZero(Number number) {
        return null != number && 0 != number.longValue();
    }


}
