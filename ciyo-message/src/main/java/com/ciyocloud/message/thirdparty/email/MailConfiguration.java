package com.ciyocloud.message.thirdparty.email;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ciyocloud.common.constant.ConfigConstants;
import com.ciyocloud.common.constant.RedisKeyConstants;
import com.ciyocloud.common.util.JsonUtils;
import com.ciyocloud.common.util.RedisUtils;
import com.ciyocloud.common.util.SpringContextUtils;
import lombok.Data;
import lombok.Getter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * @author : codeck
 * @description : 邮箱配置类
 * @create :  2021/12/28 17:13
 **/
public class MailConfiguration {

    private static final String CONFIG_KEY = StrUtil.format(RedisKeyConstants.ENV_CONFIG, ConfigConstants.EMAIL_ENV_CONFIG);
    @Getter
    private static JavaMailSender mailSender;
    @Getter
    private static Prop prop;

    static {
        buildMailSender();
    }


    public static synchronized void buildMailSender() {
        Prop properties = JsonUtils.jsonToObj(SpringContextUtils.getBean(RedisUtils.class).get(CONFIG_KEY, String.class), Prop.class);
        if (ObjectUtil.isNull(properties)) {
            return;
        }
        JavaMailSenderImpl jms = new JavaMailSenderImpl();
        jms.setHost(properties.getHost().trim());
        jms.setPort(properties.getPort());
        jms.setUsername(properties.getUsername());
        jms.setPassword(properties.getPassword());
        jms.setProtocol("smtps");
        jms.setDefaultEncoding("Utf-8");
        Properties p = new Properties();
        p.setProperty("mail.smtp.auth", "true");
        jms.setJavaMailProperties(p);
        mailSender = jms;
        prop = properties;
    }


    @Data
    static class Prop {
        private String host;
        private Integer port;
        private String username;
        private String password;
    }


}
