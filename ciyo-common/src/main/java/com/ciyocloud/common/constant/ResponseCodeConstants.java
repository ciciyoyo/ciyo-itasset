package com.ciyocloud.common.constant;

/**
 * @description: 响应状态码
 * @author: codeck
 * @create: 2020-02-10 15:46
 **/
public interface ResponseCodeConstants {
    /**
     * 接口成功
     */
    int SUCCESS = 200;
    /**
     * 接口失败 发生异常
     */
    int FAIL = 500;

    /**
     * 特殊失败 业务异常 500会统一toast提示 510前端不拦截 页面自行同理
     */
    int SPECIAL_FAIL = 510;
    /**
     * 未登录
     */
    int UNAUTHORIZED = 401;

    /**
     * 权限不足
     */
    int FORBIDDEN = 403;

    /**
     * 需要验证的请求
     */
    int NEED_VERIFICATION = 416;

    /**
     * 找不到该请求
     */
    int NOT_FOUND = 404;

    /**
     * 非法签名
     */
    Integer SIGN_FAIL_CODE = 405;

    /**
     * 非法签名
     */
    String SIGN_FAIL_MSG = "非法访问，请检查请求信息";


    String VALIDATE_CODE_FAIL_MSG = "验证码验证失败";
}
