package com.ciyocloud.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.system.entity.SysDictDataEntity;
import com.ciyocloud.system.entity.SysDictTypeEntity;

import java.util.List;

/**
 * 字典 业务层
 *
 * @author codeck
 */
public interface SysDictTypeService extends IService<SysDictTypeEntity> {


    /**
     * 根据字典类型查询信息
     *
     * @param dictType 字典类型
     * @return 字典类型
     */
    List<SysDictDataEntity> getDictDataByType(String dictType);

    /**
     * 批量删除字典类型信息
     *
     * @param dictIds 需要删除的字典ID
     * @return 结果
     */
    void deleteDictTypeByIds(List<Long> dictIds);

    /**
     * 加载字典缓存数据
     */
    void loadingDictCache();

    /**
     * 清空字典缓存数据
     */
    void clearDictCache();

    /**
     * 重置字典缓存数据
     */
    void resetDictCache();


    /**
     * 新增保存字典类型信息
     *
     * @param dict 字典类型信息
     * @return 结果
     */
    boolean saveDictType(SysDictTypeEntity dict);

    /**
     * 修改保存字典类型信息
     *
     * @param dict 字典类型信息
     * @return 结果
     */
    boolean updateDictType(SysDictTypeEntity dict);

    /**
     * 校验字典类型称是否唯一
     *
     * @param dictType 字典类型
     * @return 结果
     */
    String checkDictTypeUnique(SysDictTypeEntity dictType);
}
