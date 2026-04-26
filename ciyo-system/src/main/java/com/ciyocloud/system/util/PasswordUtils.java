package com.ciyocloud.system.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.ciyocloud.common.exception.BusinessException;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.system.service.SysConfigService;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : codeck
 * @description : 密码级别工具类
 * @create :  2022/12/12 16:32
 **/
public class PasswordUtils {


    /**
     * 正则级别和对应的表达式Map
     */
    private static final Map<String, String> REGEX_LEVEL_MAP = new HashMap<>();

    /**
     * 密码级别和对应的正则文案Map
     */
    private static final Map<String, String> LEVEL_REGEX_TEXT_MAP = new HashMap<>();
    /**
     * 缓存 RSA 对象，避免频繁创建和查询配置
     */
    private static volatile RSA cachedRsa;

    /**
     * 获取RSA加密对象
     */
    private static RSA getRsa() {
        if (cachedRsa == null) {
            synchronized (PasswordUtils.class) {
                if (cachedRsa == null) {
                    String privateKey = SpringContextUtils.getBean(SysConfigService.class).getConfigValueByKey("sys.user.password.privateKey");
                    String publicKey = SpringContextUtils.getBean(SysConfigService.class).getConfigValueByKey("sys.user.password.publicKey");

                    // 尝试从配置文件获取（如果没有从数据库获取到）
                    if (StrUtil.isEmpty(privateKey)) {
                        privateKey = SpringContextUtils.getBean(Environment.class).getProperty("password.rsa.private-key");
                    }
                    if (StrUtil.isEmpty(publicKey)) {
                        publicKey = SpringContextUtils.getBean(Environment.class).getProperty("password.rsa.public-key");
                    }

                    if (StrUtil.isEmpty(privateKey) || StrUtil.isEmpty(publicKey)) {
                        throw new BusinessException("请在后台管理系统或配置文件中配置参数: sys.user.password.privateKey 和 sys.user.password.publicKey");
                    }
                    cachedRsa = SecureUtil.rsa(privateKey, publicKey);
                }
            }
        }
        return cachedRsa;
    }

    /**
     * 清除 RSA 缓存（在后台修改密钥配置后需调用此方法）
     */
    public static void clearCache() {
        cachedRsa = null;
    }

    static {
        // 1.最基础的密码强度要求：任意5到50个字符。
        REGEX_LEVEL_MAP.put("1", ".{5,50}");
        LEVEL_REGEX_TEXT_MAP.put("1", "密码长度必须在5到20个字符之间");
        REGEX_LEVEL_MAP.put("2", "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");
        LEVEL_REGEX_TEXT_MAP.put("2", "密码至少8位，包含字母和数字，且至少有一个字母");
        REGEX_LEVEL_MAP.put("3", "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$");
        LEVEL_REGEX_TEXT_MAP.put("3", "密码至少8位，包含字母、数字和特殊字符，且至少有一个字母、一个数字和一个特殊字符");
    }

    /**
     * 检验密码是否符合规则
     * 不符合规则会抛出异常 并且异常信息为对应的错误信息
     */
    public static void checkPassword(String password) {
        String passwordLevel = getPasswordLevel();
        String reg = REGEX_LEVEL_MAP.get(passwordLevel);
        boolean match = password.matches(reg);
        if (!match) {
            throw new BusinessException(LEVEL_REGEX_TEXT_MAP.get(passwordLevel));
        }
    }

    public static void checkPassword(String password, String level) {
        String reg = REGEX_LEVEL_MAP.get(level);
        boolean match = password.matches(reg);
        if (!match) {
            throw new BusinessException(LEVEL_REGEX_TEXT_MAP.get(level));
        }
    }

    /**
     * 查询系统配置的密码级别
     * 1.
     *
     * @return 级别
     */
    public static String getPasswordLevel() {
        String value = SpringContextUtils.getBean(SysConfigService.class).getConfigValueByKey("sys.user.password.level");
        return StrUtil.isNotEmpty(value) ? value : "1";
    }

    /**
     * 解密前端传输的密码
     */
    public static String decryptPassword(String password) {
        return getRsa().decryptStr(password, KeyType.PrivateKey);
    }


}
