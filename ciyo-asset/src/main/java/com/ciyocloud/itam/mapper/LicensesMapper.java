package com.ciyocloud.itam.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ciyocloud.itam.entity.LicensesEntity;
import com.ciyocloud.itam.vo.LicenseAllocationVO;
import com.ciyocloud.itam.vo.LicensesVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 软件授权Mapper接口
 *
 * @author codeck
 * @since 2025-12-29 20:10:27
 */
public interface LicensesMapper extends BaseMapper<LicensesEntity> {

    Page<LicensesVO> selectPageVo(Page<LicensesVO> page, @Param(Constants.WRAPPER) Wrapper<LicensesEntity> queryWrapper);

    Page<LicenseAllocationVO> selectAllocationsPage(Page<LicenseAllocationVO> page, @Param("licenseId") Long licenseId);

    List<LicensesVO> selectListVo(@Param(Constants.WRAPPER) Wrapper<LicensesEntity> queryWrapper);

    List<java.util.Map<String, Object>> selectCategoryStats();
}

