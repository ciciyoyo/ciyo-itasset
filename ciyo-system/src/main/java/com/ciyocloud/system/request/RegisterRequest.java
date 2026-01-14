package com.ciyocloud.system.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册对象
 *
 * @author codeck
 */
@Data
public class RegisterRequest {


    /**
     * 用户名
     */
    @NotBlank(message = "{system.reg.name.notnull}")
    @Size(min = 1, max = 20, message = "{system.reg.name.error}")
    private String userName;

    private String nickName;

    private String password;

    @Email(message = "{system.user.email.error}")
    @Size(min = 0, max = 50, message = "{system.user.email.lenError}")
    private String email;

    /**
     * 手机号码
     */
    @Size(min = 0, max = 11, message = "{system.user.phone.lenError}")
    private String phonenumber;

    /**
     * 用户性别
     */
    private String sex;


    /**
     * 验证码code
     */
    private String code;

}
