package com.ciyocloud.datapermission.handler;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.ciyocloud.common.entity.security.SysRoleVO;
import com.ciyocloud.common.entity.security.SysUserVO;
import com.ciyocloud.common.exception.BaseException;
import com.ciyocloud.common.util.SecurityUtils;
import com.ciyocloud.datapermission.annotation.DataColumn;
import com.ciyocloud.datapermission.annotation.DataPermission;
import com.ciyocloud.datapermission.eunms.DataScopeType;
import com.ciyocloud.datapermission.helper.DataPermissionHelper;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 数据权限过滤
 *
 * @author codeck
 */
@Slf4j
public class PlusDataPermissionHandler {

    /**
     * 方法或类(名称) 与 注解的映射关系缓存
     */
    private final Map<String, DataPermission> dataPermissionCacheMap = new ConcurrentHashMap<>();

    /**
     * 无效注解方法缓存用于快速返回
     */
    private final Set<String> invalidCacheSet = new ConcurrentHashSet<>();

    /**
     * spel 解析器
     */
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParserContext parserContext = new TemplateParserContext();
    /**
     * bean解析器 用于处理 spel 表达式中对 bean 的调用
     */
    private final BeanResolver beanResolver = new BeanFactoryResolver(SpringUtil.getBeanFactory());


    public Expression getSqlSegment(Expression where, String mappedStatementId, boolean isSelect) {
        DataColumn[] dataColumns = findAnnotation(mappedStatementId);
        if (ArrayUtil.isEmpty(dataColumns)) {
            invalidCacheSet.add(mappedStatementId);
            return where;
        }
        // 如果是异步的话注入到线程变量中
        SysUserVO currentUser = DataPermissionHelper.getVariable(DataPermissionHelper.USER_KEY);
        // 没有的话从SecurityUtils中获取登录得 需要手动释放 切记
        if (ObjectUtil.isNull(currentUser)) {
            currentUser = SecurityUtils.getLoginUser().getUser();
        }
        // 如果是超级管理员，则不过滤数据
        if (SecurityUtils.isAdmin(currentUser.getId())) {
            return where;
        }
        String dataFilterSql = buildDataFilter(dataColumns, isSelect, currentUser);
        if (StrUtil.isBlank(dataFilterSql)) {
            return where;
        }
        try {
            Expression expression = CCJSqlParserUtil.parseExpression(dataFilterSql);
            // 数据权限使用单独的括号 防止与其他条件冲突
            Parenthesis parenthesis = new Parenthesis().withExpression(expression);
            if (ObjectUtil.isNotNull(where)) {
                return new AndExpression(where, parenthesis);
            } else {
                return parenthesis;
            }
        } catch (JSQLParserException e) {
            throw new BaseException("数据权限解析异常 => " + e.getMessage());
        }
    }

    /**
     * 构造数据过滤sql
     */
    private String buildDataFilter(DataColumn[] dataColumns, boolean isSelect, SysUserVO user) {
        // 更新或删除需满足所有条件
        String joinStr = isSelect ? " OR " : " AND ";
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setBeanResolver(beanResolver);
        context.setVariable("user", user);
        Set<String> conditions = new HashSet<>();
        if (null != user.getRoles()) {
            for (SysRoleVO role : user.getRoles()) {
                user.setRoleIds(CollUtil.newArrayList(role.getId()));
                // 获取角色权限泛型
                DataScopeType type = DataScopeType.findCode(role.getDataScope());
                if (ObjectUtil.isNull(type)) {
                    throw new BaseException("角色数据范围异常 => " + role.getDataScope());
                }
                // 全部数据权限直接返回
                if (type == DataScopeType.ALL) {
                    return "";
                }
                boolean isSuccess = false;
                for (DataColumn dataColumn : dataColumns) {
                    if (dataColumn.key().length != dataColumn.value().length) {
                        throw new BaseException("角色数据范围异常 => key与value长度不匹配");
                    }
                    // 不包含 key 变量 则不处理
                    if (!StrUtil.containsAny(type.getSqlTemplate(), Arrays.stream(dataColumn.key()).map(key -> "#" + key).toArray(String[]::new))) {
                        continue;
                    }
                    // 设置注解变量 key 为表达式变量 value 为变量值
                    for (int i = 0; i < dataColumn.key().length; i++) {
                        context.setVariable(dataColumn.key()[i], dataColumn.value()[i]);
                    }

                    // 解析sql模板并填充
                    String sql = parser.parseExpression(type.getSqlTemplate(), parserContext).getValue(context, String.class);
                    conditions.add(joinStr + sql);
                    isSuccess = true;
                }
                // 未处理成功则填充兜底方案
                if (!isSuccess && StrUtil.isNotBlank(type.getElseSql())) {
                    conditions.add(joinStr + type.getElseSql());
                }
            }
        }
        if (CollUtil.isNotEmpty(conditions)) {
            String sql = StreamUtil.join(conditions.stream(), "");
            return sql.substring(joinStr.length());
        }
        return "";
    }


    private DataColumn[] findAnnotation(String mappedStatementId) {
        StringBuilder sb = new StringBuilder(mappedStatementId);
        int index = sb.lastIndexOf(".");
        String clazzName = sb.substring(0, index);
        String methodName = sb.substring(index + 1, sb.length());
        Class<?> clazz = ClassUtil.loadClass(clazzName);
        List<Method> methods = Arrays.stream(ClassUtil.getDeclaredMethods(clazz)).filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());
        DataPermission dataPermission;
        // 获取方法注解
        for (Method method : methods) {
            dataPermission = dataPermissionCacheMap.get(mappedStatementId);
            if (ObjectUtil.isNotNull(dataPermission)) {
                return dataPermission.value();
            }
            if (AnnotationUtil.hasAnnotation(method, DataPermission.class)) {
                dataPermission = AnnotationUtil.getAnnotation(method, DataPermission.class);
                dataPermissionCacheMap.put(mappedStatementId, dataPermission);
                return dataPermission.value();
            }
        }
        dataPermission = dataPermissionCacheMap.get(clazz.getName());
        if (ObjectUtil.isNotNull(dataPermission)) {
            return dataPermission.value();
        }
        // 从类获取注解
        if (AnnotationUtil.hasAnnotation(clazz, DataPermission.class)) {
            dataPermission = AnnotationUtil.getAnnotation(clazz, DataPermission.class);
            dataPermissionCacheMap.put(clazz.getName(), dataPermission);
            return dataPermission.value();
        }
        return null;
    }

    /**
     * 是否为无效方法 无数据权限
     */
    public boolean isInvalid(String mappedStatementId) {
        return invalidCacheSet.contains(mappedStatementId);
    }
}
