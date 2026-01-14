package com.ciyocloud.system.service.other;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.stream.StreamUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ciyocloud.common.helper.DataBaseHelper;
import com.ciyocloud.common.util.StreamUtils;
import com.ciyocloud.system.entity.SysDeptEntity;
import com.ciyocloud.system.entity.SysRoleDeptEntity;
import com.ciyocloud.system.mapper.SysDeptMapper;
import com.ciyocloud.system.mapper.SysRoleDeptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据权限 实现
 * 注意: 此Service内不允许调用标注`数据权限`注解的方法
 * 例如: deptMapper.selectList 此 selectList 方法标注了`数据权限`注解 会出现循环解析的问题
 *
 * @author codeck
 */
@RequiredArgsConstructor
@Service("sdss")
public class SysDataScopeService {

    private final SysRoleDeptMapper roleDeptMapper;
    private final SysDeptMapper deptMapper;


    /**
     * 获取角色关联的部门
     *
     * @param roleIds 角色
     * @return 部门id ，分割
     */
    public String getRoleCustom(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return "";
        }
        List<SysRoleDeptEntity> list = roleDeptMapper.selectList(new LambdaQueryWrapper<SysRoleDeptEntity>()
                .select(SysRoleDeptEntity::getDeptId)
                .in(SysRoleDeptEntity::getRoleId, roleIds.get(0)));
        if (CollUtil.isNotEmpty(list)) {
            return StreamUtil.join(list.stream(), ",", rd -> Convert.toStr(rd.getDeptId()));
        }
        return null;
    }


    /**
     * 获取部门及子部门
     *
     * @param deptId 部门id
     * @return 部门id ，分割
     */
    public String getDeptAndChild(Long deptId) {
        List<SysDeptEntity> deptList = deptMapper.selectList(new LambdaQueryWrapper<SysDeptEntity>()
                .select(SysDeptEntity::getId)
                .apply(DataBaseHelper.findInSet(deptId, "ancestors")));
        List<Long> ids = StreamUtils.toList(deptList, SysDeptEntity::getId);
        ids.add(deptId);
        List<SysDeptEntity> list = deptMapper.selectList(new LambdaQueryWrapper<SysDeptEntity>()
                .select(SysDeptEntity::getId)
                .in(SysDeptEntity::getId, ids));
        if (CollUtil.isNotEmpty(list)) {
            return StreamUtils.join(list, d -> Convert.toStr(d.getId()));
        }
        return null;
    }

}
