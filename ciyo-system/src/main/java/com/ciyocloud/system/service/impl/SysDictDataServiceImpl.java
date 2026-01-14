package com.ciyocloud.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.system.constant.SystemConstants;
import com.ciyocloud.system.entity.SysDictDataEntity;
import com.ciyocloud.system.mapper.SysDictDataMapper;
import com.ciyocloud.system.service.SysDictDataService;
import com.ciyocloud.system.util.DictUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典 业务层处理
 *
 * @author codeck
 */
@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictDataEntity> implements SysDictDataService {
    private final SysDictDataMapper dictDataMapper;

    /**
     * 根据字典类型和字典键值查询字典数据信息
     *
     * @param dictType  字典类型
     * @param dictValue 字典键值
     * @return 字典标签
     */
    @Override
    public String getDictLabel(String dictType, String dictValue) {
        SysDictDataEntity data = dictDataMapper.selectOne(Wrappers.<SysDictDataEntity>lambdaQuery()
                .eq(SysDictDataEntity::getDictType, dictType).eq(SysDictDataEntity::getDictValue, dictValue)
                .select(SysDictDataEntity::getDictLabel));
        if (ObjectUtil.isNotNull(data)) {
            return data.getDictLabel();
        }
        return StrUtil.EMPTY;
    }


    /**
     * 根据字典类型获取
     *
     * @param dictType
     */
    @Override
    public List<SysDictDataEntity> getDictDataList(String dictType) {
        List<SysDictDataEntity> dictDatas = dictDataMapper.selectList(Wrappers.<SysDictDataEntity>lambdaQuery()
                .eq(SysDictDataEntity::getDictType, dictType)
                .eq(SysDictDataEntity::getStatus, SystemConstants.NORMAL_STATUS)
                .orderByAsc(SysDictDataEntity::getDictSort));
        return dictDatas;
    }

    /**
     * 批量删除字典数据信息
     *
     * @param dictCodes 需要删除的字典数据ID
     * @return 结果
     */
    @Override
    public void deleteDictDataByIds(List<Long> dictCodes) {
        for (Long dictCode : dictCodes) {
            SysDictDataEntity data = getById(dictCode);
            int row = dictDataMapper.deleteById(dictCode);
            this.setDictCache(data, row);
        }
    }

    /**
     * 新增保存字典数据信息
     *
     * @param data 字典数据信息
     * @return 结果
     */
    @Override
    public int saveDictData(SysDictDataEntity data) {
        int row = dictDataMapper.insert(data);
        setDictCache(data, row);
        return row;
    }

    /**
     * 修改保存字典数据信息
     *
     * @param data 字典数据信息
     * @return 结果
     */
    @Override
    public int updateDictData(SysDictDataEntity data) {
        int row = dictDataMapper.updateById(data);
        setDictCache(data, row);
        return row;
    }


    /**
     * 设置缓存
     *
     * @param data
     * @param row
     * @return
     */
    private void setDictCache(SysDictDataEntity data, int row) {
        if (row > 0) {
            List<SysDictDataEntity> dictDatas = dictDataMapper.selectList(Wrappers.<SysDictDataEntity>lambdaQuery()
                    .eq(SysDictDataEntity::getDictType, data.getDictType()).eq(SysDictDataEntity::getStatus, SystemConstants.NORMAL_STATUS)
                    .orderByAsc(SysDictDataEntity::getDictSort));
            DictUtils.setDictCache(data.getDictType(), dictDatas);
        }

    }
}



