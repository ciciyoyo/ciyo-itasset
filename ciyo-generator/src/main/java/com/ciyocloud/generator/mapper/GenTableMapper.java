package com.ciyocloud.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.generator.entity.GenTableEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 业务 数据层
 *
 * @author Lion Li
 */
@InterceptorIgnore(dataPermission = "true")
public interface GenTableMapper extends BaseMapper<GenTableEntity> {

    /**
     * 查询据库列表
     *
     * @param genTable 查询条件
     * @return 数据库表集合
     */
    Page<GenTableEntity> selectPageDbTableList(@Param("page") Page<GenTableEntity> page, @Param("genTable") GenTableEntity genTable);

    /**
     * 查询据库列表
     *
     * @param tableNames 表名称组
     * @return 数据库表集合
     */
    List<GenTableEntity> selectDbTableListByNames(@Param("tableNames") String[] tableNames);

    /**
     * 查询所有表信息
     *
     * @return 表信息集合
     */
    List<GenTableEntity> selectGenTableAll();

    /**
     * 查询表ID业务信息
     *
     * @param id 业务ID
     * @return 业务信息
     */
    GenTableEntity selectGenTableById(Long id);

    /**
     * 查询表名称业务信息
     *
     * @param tableName 表名称
     * @return 业务信息
     */
    GenTableEntity selectGenTableByName(String tableName);

}
