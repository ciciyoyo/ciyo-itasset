package com.ciyocloud.itam.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.itam.entity.DeviceEntity;
import com.ciyocloud.itam.vo.DeviceVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 设备管理Mapper接口
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
public interface DeviceMapper extends BaseMapper<DeviceEntity> {

    /**
     * 分页查询设备列表
     *
     * @param page         分页参数
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    Page<DeviceVO> selectPageVo(@Param("page") Page<DeviceVO> page, @Param(Constants.WRAPPER) Wrapper<DeviceEntity> queryWrapper);

    /**
     * 查询设备列表
     *
     * @param queryWrapper 查询条件
     * @return 结果
     */
    List<DeviceVO> selectListVo(@Param(Constants.WRAPPER) Wrapper<DeviceEntity> queryWrapper);


    /**
     * 统计设备汇总数据
     *
     * @param now      当前日期
     * @param soonDate 即将到期日期
     * @return 统计结果
     */
    @Select("SELECT " +
            "COUNT(*) as total, " +
            "COUNT(CASE WHEN assets_status = 4 THEN 1 END) as scrapped, " +
            "COUNT(CASE WHEN warranty_date <= #{now} THEN 1 END) as expired, " +
            "COUNT(CASE WHEN warranty_date > #{now} AND warranty_date <= #{soonDate} THEN 1 END) as soonToExpire " +
            "FROM itam_device " +
            "WHERE deleted = 0")
    Map<String, Object> selectSummaryStats(@Param("now") LocalDate now, @Param("soonDate") LocalDate soonDate);
}
