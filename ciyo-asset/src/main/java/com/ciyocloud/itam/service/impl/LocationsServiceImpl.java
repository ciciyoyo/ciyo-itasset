package com.ciyocloud.itam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.common.util.QueryWrapperUtils;
import com.ciyocloud.itam.entity.LocationsEntity;
import com.ciyocloud.itam.mapper.LocationsMapper;
import com.ciyocloud.itam.service.LocationsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 物理位置Service业务层处理
 *
 * @author codeck
 * @since 2025-12-29 15:58:21
 */
@Service
public class LocationsServiceImpl extends ServiceImpl<LocationsMapper, LocationsEntity> implements LocationsService {


    @Override
    public List<LocationsEntity> getTree(LocationsEntity locations) {
        List<LocationsEntity> entities = this.list(QueryWrapperUtils.toSimpleQuery(locations));
        List<Long> entityIds = entities.stream().map(LocationsEntity::getId).collect(Collectors.toList());
        return entities.stream()
                .filter(e -> e.getParentId() == null || e.getParentId() == 0 || !entityIds.contains(e.getParentId()))
                .peek(e -> e.setChildren(getChildren(e, entities)))
                .collect(Collectors.toList());
    }

    /**
     * 递归获取子节点
     *
     * @param root 父节点
     * @param all  所有节点
     * @return 子节点列表
     */
    private List<LocationsEntity> getChildren(LocationsEntity root, List<LocationsEntity> all) {
        return all.stream()
                .filter(e -> Objects.equals(e.getParentId(), root.getId()))
                .peek(e -> e.setChildren(getChildren(e, all)))
                .collect(Collectors.toList());
    }
}
