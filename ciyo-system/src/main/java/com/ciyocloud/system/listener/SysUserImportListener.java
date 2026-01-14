package com.ciyocloud.system.listener;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.common.util.SpringContextUtils;
import com.ciyocloud.excel.core.ExcelListener;
import com.ciyocloud.excel.core.ExcelResult;
import com.ciyocloud.system.constant.UserConstants;
import com.ciyocloud.system.entity.SysUserEntity;
import com.ciyocloud.system.service.SysConfigService;
import com.ciyocloud.system.service.SysUserService;
import com.ciyocloud.system.util.PasswordUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.ciyocloud.system.constant.SysConfigConstants.DEFAULT_PASSWORD;

/**
 * 系统用户自定义导入
 *
 * @author Lion Li
 */
@Slf4j
public class SysUserImportListener extends AnalysisEventListener<SysUserEntity> implements ExcelListener<SysUserEntity> {

    private final SysUserService userService;

    private final String password;

    private final Boolean isUpdateSupport;

    private final Long operName;
    private final StringBuilder successMsg = new StringBuilder();
    private final StringBuilder failureMsg = new StringBuilder();
    private int successNum = 0;
    private int failureNum = 0;

    public SysUserImportListener(Boolean isUpdateSupport) {
        String initPassword = SpringContextUtils.getBean(SysConfigService.class).getConfigValueByKey(DEFAULT_PASSWORD);
        this.userService = SpringContextUtils.getBean(SysUserService.class);
        this.password = SecurityUtils.encryptPassword(initPassword);
        this.isUpdateSupport = isUpdateSupport;
        this.operName = SecurityUtils.getUserId();
    }

    @Override
    public void invoke(SysUserEntity userVo, AnalysisContext context) {
        String passwordLevel = PasswordUtils.getPasswordLevel();
        if (StrUtil.isNotBlank(userVo.getPassword())) {
            try {
                PasswordUtils.checkPassword(userVo.getPassword(), passwordLevel);
            } catch (Exception e) {
                failureNum++;
                failureMsg.append("<br/>").append(failureNum).append("、账号 ").append(userVo.getUserName()).append(" 导入失败，")
                        .append(e.getMessage());
                return;
            }
        }
        SysUserEntity user = this.userService.getUserByUserName(userVo.getUserName());
        try {
            // 验证是否存在这个用户
            if (ObjectUtil.isNull(user)) {
                user = BeanUtil.toBean(userVo, SysUserEntity.class);
                if (StrUtil.isNotBlank(userVo.getPassword())) {
                    user.setPassword(SecurityUtils.encryptPassword(userVo.getPassword()));
                } else {
                    user.setPassword(this.password);
                }
                user.setCreateBy(operName);
                if (StrUtil.isNotEmpty(user.getPhonenumber()) && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、账号 ").append(user.getUserName()).append(" '导入失败，手机号码已存在");
                    return;
                } else if (StrUtil.isNotEmpty(user.getEmail()) && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、账号 ").append(user.getUserName()).append(" '导入失败，邮箱账号已存在");
                    return;
                }
                userService.insertUser(user);
                successNum++;
                successMsg.append("<br/>").append(successNum).append("、账号 ").append(user.getUserName()).append(" 导入成功");
            } else if (isUpdateSupport) {
                userVo.setId(user.getId());
                userVo.setUpdateBy(operName);
                if (StrUtil.isNotBlank(userVo.getPassword())) {
                    userVo.setPassword(SecurityUtils.encryptPassword(userVo.getPassword()));
                }
                if (StrUtil.isNotEmpty(user.getPhonenumber())
                        && !StrUtil.equals(user.getPhonenumber(), userVo.getPhonenumber())
                        && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、账号 ").append(user.getUserName()).append(" '导入失败，手机号码已存在");
                    return;
                } else if (StrUtil.isNotEmpty(user.getEmail())
                        && !StrUtil.equals(user.getEmail(), userVo.getEmail())
                        && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、账号 ").append(user.getUserName()).append(" '导入失败，邮箱账号已存在");
                    return;
                }
                userService.updateUser(userVo);
                successNum++;
                successMsg.append("<br/>").append(successNum).append("、账号 ").append(user.getUserName()).append(" 更新成功");
            } else {
                failureNum++;
                failureMsg.append("<br/>").append(failureNum).append("、账号 ").append(user.getUserName()).append(" 已存在");
            }
        } catch (Exception e) {
            failureNum++;
            String msg = "<br/>" + failureNum + "、账号 " + user.getUserName() + " 导入失败：";
            failureMsg.append(msg).append(e.getMessage());
            log.error(msg, e);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }

    @Override
    public ExcelResult<SysUserEntity> getExcelResult() {
        return new ExcelResult<SysUserEntity>() {

            @Override
            public String getAnalysis() {
                if (failureNum > 0) {
                    failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
                    throw new RuntimeException(failureMsg.toString());
                } else {
                    successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
                }
                return successMsg.toString();
            }

            @Override
            public List<SysUserEntity> getList() {
                return null;
            }

            @Override
            public List<String> getErrorList() {
                return null;
            }
        };
    }
}
