package com.ciyocloud.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ciyocloud.system.entity.SysPostEntity;

import java.util.List;

/**
 * 岗位信息 数据层
 *
 * @author codeck
 */
public interface SysPostMapper extends BaseMapper<SysPostEntity> {


    /***
     * 检查名字是否存在
     * @param postName
     * @return
     */
    SysPostEntity checkPostNameUnique(String postName);

    /**
     * 检查code是否唯一
     *
     * @param postCode
     * @return
     */
    SysPostEntity checkPostCodeUnique(String postCode);

    /**
     * 查询岗位
     *
     * @param userName
     * @return
     */
    List<SysPostEntity> selectPostsByUserName(String userName);
}
