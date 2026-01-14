package com.ciyocloud.system.service.impl;

import cn.hutool.core.img.Img;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.common.exception.BaseException;
import com.ciyocloud.system.constant.UserConstants;
import com.ciyocloud.system.entity.SysDictDataEntity;
import com.ciyocloud.system.entity.SysDictTypeEntity;
import com.ciyocloud.system.mapper.SysDictTypeMapper;
import com.ciyocloud.system.service.SysDictDataService;
import com.ciyocloud.system.service.SysDictTypeService;
import com.ciyocloud.system.util.DictUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * 字典 业务层处理
 *
 * @author codeck
 */
@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictTypeEntity> implements SysDictTypeService {


    private final SysDictDataService sysDictDataService;

    public static void main(String[] args) {
//        HttpUtil.downloadFile("http://demo.ccsmec.com/image/auth/2022/3/4/aed5afe4-cf06-4960-860b-37eec14f46ef.jpg", FileUtil.file("D:\\46ef.jpg"));
        int height = ImgUtil.toBufferedImage(
                Img.from(new ByteArrayInputStream(HttpUtil.downloadBytes("http://demo.ccsmec.com/image/auth/2022/3/4/aed5afe4-cf06-4960-860b-37eec14f46ef.jpg"))).getImg()).getHeight();
        System.out.println(height);
    }

    /**
     * 项目启动时，初始化字典到缓存
     */
    @PostConstruct
    public void init() {
        loadingDictCache();
    }

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    @Override
    public List<SysDictDataEntity> getDictDataByType(String dictType) {
        List<SysDictDataEntity> dictDatas = DictUtils.getDictCache(dictType);
        if (ObjectUtil.isNotEmpty(dictDatas)) {
            return dictDatas;
        }
        dictDatas = sysDictDataService.list(Wrappers.<SysDictDataEntity>lambdaQuery().eq(SysDictDataEntity::getDictType, dictType));
        if (ObjectUtil.isNotEmpty(dictDatas)) {
            DictUtils.setDictCache(dictType, dictDatas);
            return dictDatas;
        }
        return null;
    }

    /**
     * 批量删除字典类型信息
     *
     * @param dictIds 需要删除的字典ID
     * @return 结果
     */
    @Override
    public void deleteDictTypeByIds(List<Long> dictIds) {
        for (Long dictId : dictIds) {
            SysDictTypeEntity dictType = this.getById(dictId);
            if (sysDictDataService.count(Wrappers.<SysDictDataEntity>lambdaQuery().eq(SysDictDataEntity::getDictType, dictType.getDictType())) > 0) {
                throw new BaseException(String.format("%1$s已分配,不能删除", dictType.getDictName()));
            }
            sysDictDataService.removeById(dictId);
            DictUtils.removeDictCache(dictType.getDictType());
        }
    }

    /**
     * 加载字典缓存数据
     */
    @Override
    public void loadingDictCache() {
        List<SysDictTypeEntity> dictTypeList = this.list();
        for (SysDictTypeEntity dictType : dictTypeList) {
            List<SysDictDataEntity> dictDatas = sysDictDataService.getDictDataList(dictType.getDictType());
            DictUtils.setDictCache(dictType.getDictType(), dictDatas);
        }
    }

    /**
     * 清空字典缓存数据
     */
    @Override
    public void clearDictCache() {
        DictUtils.clearDictCache();
    }

    /**
     * 重置字典缓存数据
     */
    @Override
    public void resetDictCache() {
        clearDictCache();
        loadingDictCache();
    }

    /**
     * 新增保存字典类型信息
     *
     * @param dict 字典类型信息
     * @return 结果
     */
    @Override
    public boolean saveDictType(SysDictTypeEntity dict) {
        boolean flag = this.save(dict);
        if (flag) {
            DictUtils.setDictCache(dict.getDictType(), null);
        }
        return flag;
    }

    /**
     * 修改保存字典类型信息
     *
     * @param dict 字典类型信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public boolean updateDictType(SysDictTypeEntity dict) {
        SysDictTypeEntity oldDict = this.getById(dict.getId());
        sysDictDataService.update(new SysDictDataEntity() {{
            setDictType(dict.getDictType());
        }}, Wrappers.<SysDictDataEntity>lambdaUpdate().eq(SysDictDataEntity::getDictType, oldDict.getDictType()));
        boolean flag = this.updateById(dict);
        if (flag) {
            List<SysDictDataEntity> dictDatas = sysDictDataService.getDictDataList(dict.getDictType());
            DictUtils.setDictCache(dict.getDictType(), dictDatas);
        }
        return flag;
    }

    /**
     * 校验字典类型称是否唯一
     *
     * @param dict 字典类型
     * @return 结果
     */
    @Override
    public String checkDictTypeUnique(SysDictTypeEntity dict) {
        Long dictId = ObjectUtil.isNull(dict.getId()) ? -1L : dict.getId();
        SysDictTypeEntity dictType = this.getOne(Wrappers.<SysDictTypeEntity>lambdaQuery()
                .eq(SysDictTypeEntity::getDictType, dict.getDictType()));
        if (ObjectUtil.isNotNull(dictType) && dictType.getId().longValue() != dictId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }
}
