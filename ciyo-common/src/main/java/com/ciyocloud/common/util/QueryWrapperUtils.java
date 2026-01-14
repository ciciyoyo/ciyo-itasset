package com.ciyocloud.common.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ciyocloud.common.entity.BaseEntity;
import com.ciyocloud.common.entity.IDictEnum;
import com.ciyocloud.common.helper.DataBaseHelper;

import java.beans.PropertyDescriptor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author : codeck
 * @description : mybatis wrapeprs 工具类
 * @create :  2021/07/12 16:34
 **/
public class QueryWrapperUtils {


    /**
     * 生成简单查询wrapper
     * 对于参数String默认使用like
     * 对于Long Boolean Integer 使用eq等值
     *
     * @param searchObj
     */
    public static <T> QueryWrapper<T> toSimpleQuery(T searchObj) {
        return toSimpleQuery(searchObj, (Class<T>) searchObj.getClass());
    }

    /**
     * 生成简单查询wrapper
     *
     * @param searchObj   搜索对象
     * @param targetClass 目标Entity类
     * @param <T>         搜索对象类型
     * @param <R>         目标Entity类型
     * @return QueryWrapper
     */
    public static <T, R> QueryWrapper<R> toSimpleQuery(T searchObj, Class<R> targetClass) {
        QueryWrapper<R> queryWrapper = new QueryWrapper<R>();
        PropertyDescriptor[] propertyDescriptors = BeanUtil.getPropertyDescriptors(searchObj.getClass());
        for (PropertyDescriptor property : propertyDescriptors) {
            Object fieldValue = BeanUtil.getFieldValue(searchObj, property.getName());
            String name = property.getName();
            // 如果值非空 进行默认查询方式 string 为模糊 数值为=查询
            if (ObjectUtil.isNotNull(fieldValue)) {
                if (fieldValue instanceof String) {
                    queryWrapper.like(StrUtil.toUnderlineCase(name), fieldValue);
                } else if (fieldValue instanceof Long || fieldValue instanceof Boolean || fieldValue instanceof Integer) {
                    queryWrapper.eq(StrUtil.toUnderlineCase(name), fieldValue);
                } else if (fieldValue instanceof IDictEnum) {
                    queryWrapper.eq(StrUtil.toUnderlineCase(name), ((IDictEnum) fieldValue).getValue());
                }
            }
        }
        Map<String, Object> parameterMap = null;
        Object params = ReflectUtil.getFieldValue(searchObj, "params");
        if (ObjectUtil.isNotNull(params) && params instanceof Map) {
            parameterMap = (Map) params;
        } else {
            // 添加默认排序
            if (BaseEntity.class.isAssignableFrom(targetClass)) {
                queryWrapper.orderByDesc(StrUtil.toUnderlineCase(BaseEntity.Fields.createTime));
            }
            return queryWrapper;
        }
        addDateTimeRange(parameterMap, queryWrapper);
        //处理排序
        handleSortColumn(queryWrapper, parameterMap);
        return queryWrapper.checkSqlInjection();
    }

    /**
     * 排序处理
     *
     * @param params
     * @return
     */
    public static <T> void handleSortColumn(QueryWrapper<T> queryWrapper, Map<String, Object> params) {
        if (ObjectUtil.isNull(params)) {
            // 默认根据创建时间倒序
            queryWrapper.orderByDesc(StrUtil.toUnderlineCase(BaseEntity.Fields.createTime));
            return;
        }
        String orderByColumn = MapUtil.getStr(params, "orderByColumn");
        String isAsc = MapUtil.getStr(params, "isAsc");
        if (StrUtil.isBlank(orderByColumn)) {
            queryWrapper.orderByDesc(DataBaseHelper.convertField(StrUtil.toUnderlineCase(BaseEntity.Fields.createTime)));
            return;
        }
        queryWrapper.orderBy(StrUtil.isNotBlank(orderByColumn) && StrUtil.isNotBlank(isAsc),
                isAsc.equals("ascending"), DataBaseHelper.convertField(StrUtil.toUnderlineCase(orderByColumn)));
    }


    /**
     * 添加时间范围
     */
    public static void addDateTimeRange(String field, Map<String, Object> params, QueryWrapper queryWrapper) {
        if (ObjectUtil.isNull(params)) {
            return;
        }
        String beginTime = MapUtil.getStr(params, "beginTime");
        String endTime = MapUtil.getStr(params, "endTime");
        if (StrUtil.isBlank(beginTime) || StrUtil.isBlank(endTime)) {
            return;
        }
        LocalDateTime beginDateTime = LocalDateTimeUtil.beginOfDay(LocalDateTimeUtil.parse(beginTime, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        LocalDateTime endDateTime = LocalDateTimeUtil.endOfDay(LocalDateTimeUtil.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        queryWrapper.between(field, beginDateTime, endDateTime);
    }

    /**
     * 添加时间范围
     */
    public static void addDateTimeRange(Map<String, Object> params, QueryWrapper queryWrapper) {
        addDateTimeRange("create_time", params, queryWrapper);
    }

}
