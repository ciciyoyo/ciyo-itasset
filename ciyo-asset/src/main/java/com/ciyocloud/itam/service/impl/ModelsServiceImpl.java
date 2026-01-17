package com.ciyocloud.itam.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.itam.entity.ModelsEntity;
import com.ciyocloud.itam.mapper.ModelsMapper;
import com.ciyocloud.itam.service.ModelsService;
import com.ciyocloud.itam.vo.ModelsVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 型号Service业务层处理
 *
 * @author codeck
 * @since 2026-01-01
 */
@Service
public class ModelsServiceImpl extends ServiceImpl<ModelsMapper, ModelsEntity> implements ModelsService {

    @Override
    public Page<ModelsVO> selectPageVo(IPage<ModelsVO> page, Wrapper<ModelsEntity> queryWrapper) {
        return baseMapper.selectPageVo(page, queryWrapper);
    }

    @Override
    public Page<ModelsVO> selectPageVo(IPage<ModelsVO> page, ModelsEntity models) {
        return baseMapper.selectPageVo(page, buildQueryWrapper(models));
    }

    @Override
    public List<ModelsVO> selectListVo(Wrapper<ModelsEntity> queryWrapper) {
        return baseMapper.selectListVo(queryWrapper);
    }

    @Override
    public List<ModelsVO> selectListVo(ModelsEntity models) {
        return baseMapper.selectListVo(buildQueryWrapper(models));
    }

    /**
     * 构建查询条件（支持关联表查询）
     *
     * @param modelsEntity 查询参数实体
     * @return QueryWrapper
     */
    public QueryWrapper<ModelsEntity> buildQueryWrapper(ModelsEntity modelsEntity) {
        QueryWrapper<ModelsEntity> wrapper = new QueryWrapper<>();

        if (ObjectUtil.isNull(modelsEntity)) {
            wrapper.orderByDesc("t1.create_time");
            return wrapper;
        }

        // 主表字段查询
        // 型号名称 - 模糊查询
        if (StrUtil.isNotBlank(modelsEntity.getName())) {
            wrapper.like("t1.name", modelsEntity.getName());
        }

        // 型号编码 - 模糊查询
        if (StrUtil.isNotBlank(modelsEntity.getModelNumber())) {
            wrapper.like("t1.model_number", modelsEntity.getModelNumber());
        }

        // 厂商ID - 精确查询
        if (ObjectUtil.isNotNull(modelsEntity.getManufacturerId())) {
            wrapper.eq("t1.manufacturer_id", modelsEntity.getManufacturerId());
        }

        // 分类ID - 精确查询
        if (ObjectUtil.isNotNull(modelsEntity.getCategoryId())) {
            wrapper.eq("t1.category_id", modelsEntity.getCategoryId());
        }

        // 折旧规则ID - 精确查询
        if (ObjectUtil.isNotNull(modelsEntity.getDepreciationId())) {
            wrapper.eq("t1.depreciation_id", modelsEntity.getDepreciationId());
        }

        // 报废年限 - 精确查询
        if (ObjectUtil.isNotNull(modelsEntity.getEol())) {
            wrapper.eq("t1.eol", modelsEntity.getEol());
        }
        wrapper.eq("t1.deleted", 0);
        wrapper.orderByDesc("t1.create_time");

        return wrapper;
    }

    /**
     * 获取排序字段（处理表别名）
     */
    private String getSortColumn(String column) {
        String underlineColumn = StrUtil.toUnderlineCase(column);

        // 关联表字段
        if ("category_name".equals(underlineColumn)) {
            return "t2.name";
        } else if ("manufacturer_name".equals(underlineColumn)) {
            return "t3.name";
        } else if ("depreciation_name".equals(underlineColumn)) {
            return "t4.name";
        }

        // 主表字段
        return "t1." + underlineColumn;
    }
}
