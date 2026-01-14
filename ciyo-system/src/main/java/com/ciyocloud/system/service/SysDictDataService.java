package com.ciyocloud.system.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.system.entity.SysDictDataEntity;

import java.util.List;

/**
 * 字典 业务层
 *
 * @author codeck
 */
public interface SysDictDataService extends IService<SysDictDataEntity> {

    /**
     * 根据字典类型和字典键值查询字典数据信息
     *
     * @param dictType  字典类型
     * @param dictValue 字典键值
     * @return 字典标签
     */
    String getDictLabel(String dictType, String dictValue);

    /**
     * 根据字典类型获取信息
     *
     * @param dictType
     * @return
     */
    List<SysDictDataEntity> getDictDataList(String dictType);

    /**
     * 批量删除字典数据信息
     *
     * @param dictCodes 需要删除的字典数据ID
     * @return 结果
     */
    void deleteDictDataByIds(List<Long> dictCodes);

    /**
     * 新增保存字典数据信息
     *
     * @param data 字典数据信息
     * @return 结果
     */
    int saveDictData(SysDictDataEntity data);

    /**
     * 修改保存字典数据信息
     *
     * @param data 字典数据信息
     * @return 结果
     */
    int updateDictData(SysDictDataEntity data);
}
