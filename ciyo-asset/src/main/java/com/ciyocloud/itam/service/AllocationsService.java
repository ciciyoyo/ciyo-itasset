package com.ciyocloud.itam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.itam.entity.AllocationsEntity;
import com.ciyocloud.itam.enums.AllocationOwnerType;
import com.ciyocloud.itam.enums.AssetType;
import com.ciyocloud.itam.req.AllocationsPageReq;
import com.ciyocloud.itam.vo.AllocationsVO;

import java.util.List;

/**
 * 资源分配/领用明细表 服务类
 *
 * @author codeck
 * @since 2025-12-30
 */
public interface AllocationsService extends IService<AllocationsEntity> {

    /**
     * 创建分配记录
     *
     * @param itemType  资源类型
     * @param itemId    资源ID
     * @param ownerType 归属类型
     * @param ownerId   归属ID
     * @param quantity  数量
     * @param note      备注
     * @return 是否成功
     */
    boolean createAllocation(AssetType itemType, Long itemId, AllocationOwnerType ownerType, Long ownerId, Integer quantity, String note);

    /**
     * 关闭分配记录 (归还)
     *
     * @param itemType 资源类型
     * @param itemId   资源ID
     * @return 是否成功
     */
    boolean closeAllocation(AssetType itemType, Long itemId);

    /**
     * 关闭分配记录 (归还)
     *
     * @param id 分配记录ID
     * @return 是否成功
     */
    boolean closeAllocation(Long id);

    /**
     * 资源分配
     *
     * @param allocation 分配信息
     * @return 是否成功
     */
    boolean allocate(AllocationsEntity allocation);

    /**
     * 资源取消分配 (归还)
     *
     * @param id 分配记录ID
     * @return 是否成功
     */
    boolean deallocate(Long id);

    /**
     * 分页查询分配记录VO
     *
     * @param page 分页对象
     * @param req  查询条件
     * @return 分页VO
     */
    Page<AllocationsVO> queryPageVo(Page<AllocationsVO> page, AllocationsPageReq req);

    /**
     * 查询分配记录VO列表
     *
     * @param req 查询条件
     * @return VO列表
     */
    List<AllocationsVO> queryListVo(AllocationsPageReq req);
}
