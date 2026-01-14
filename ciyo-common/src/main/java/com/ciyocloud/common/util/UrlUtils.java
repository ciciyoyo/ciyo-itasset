package com.ciyocloud.common.util;

import lombok.experimental.UtilityClass;

/**
 * 链接工具类
 */
@UtilityClass
public class UrlUtils {

    /**
     * 拼接url 如果参数1的结尾和参数2的开始都是/ 截取掉一个
     *
     * @param url1 参数1
     * @param url2 参数2
     * @return 拼接后的url
     */
    public String joinUrl(String url1, String url2) {
        if (url1.endsWith("/") && url2.startsWith("/")) {
            url1 = url1.substring(0, url1.length() - 1);
        }
        // 如果两个都没有/ 那么加一个/
        if (!url1.endsWith("/") && !url2.startsWith("/")) {
            url1 = url1 + "/";
        }
        return url1 + url2;
    }
}
