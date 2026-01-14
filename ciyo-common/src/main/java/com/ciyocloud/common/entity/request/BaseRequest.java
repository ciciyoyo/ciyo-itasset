package com.ciyocloud.common.entity.request;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 基础请求对象
 *
 * @author codeck
 */
@Data
public class BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 请求参数
     */
    private Map<String, Object> params;

    public Map<String, Object> getParams() {
        if (params == null) {
            params = new HashMap<>();
        }
        return params;
    }
}
