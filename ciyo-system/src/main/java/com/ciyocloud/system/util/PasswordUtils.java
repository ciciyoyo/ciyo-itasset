package com.ciyocloud.system.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.ciyocloud.common.exception.BusinessException;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.system.service.SysConfigService;

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
    private final static String privateKey = "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEArFSp7f9RcrewO1Mw\n" +
            "Pyrpc1a9UhOdnGAZiu42hZJeu++QZ+Bb9KzwF9dOFEwAv+aIhGrwbr7XERdMFMR2\n" +
            "D8MEoQIDAQABAkBtUSOegtjC4b0cVPAFEg6XCM39082mVdu+ItBZOPl5Uzo9BHRD\n" +
            "xnr5oTkcCRDB+mZELkioeUFa/uqivTaqzDkBAiEA441k+Votl6zXaez6kV9uVXQC\n" +
            "fBKPgOEAZFTP603p9LECIQDB3/O4UbtWLQN1Tf6Tj19zGI7AS0BaEmgw6cx4bnDK\n" +
            "8QIgavOjOPvkn/ySBuxmXPuArVNoc455unaGq6GdVBh71RECIHqSdmn/8mrHRrpx\n" +
            "NxRfvr7rtcTJTsQjgw/5oLY7TMBhAiEA2zk34VyWtKPRUdfFWAHjQoyQ2DJtqvs4\n" +
            "xrYzu/CSTbU=";
    private final static String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKxUqe3/UXK3sDtTMD8q6XNWvVITnZxg\n" +
            "GYruNoWSXrvvkGfgW/Ss8BfXThRMAL/miIRq8G6+1xEXTBTEdg/DBKECAwEAAQ==";

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
        RSA rsa = SecureUtil.rsa(privateKey, publicKey);
        return rsa.decryptStr(password, KeyType.PrivateKey);
    }


}
