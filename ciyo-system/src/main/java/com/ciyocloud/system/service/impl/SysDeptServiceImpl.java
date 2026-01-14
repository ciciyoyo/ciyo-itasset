package com.ciyocloud.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.common.entity.BaseEntity;
import com.ciyocloud.common.exception.BaseException;
import com.ciyocloud.system.constant.SystemConstants;
import com.ciyocloud.system.constant.UserConstants;
import com.ciyocloud.system.entity.SysDeptEntity;
import com.ciyocloud.system.entity.SysRoleEntity;
import com.ciyocloud.system.mapper.SysDeptMapper;
import com.ciyocloud.system.mapper.SysRoleMapper;
import com.ciyocloud.system.service.SysDeptService;
import com.ciyocloud.system.vo.TreeSelectVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门管理 服务实现
 *
 * @author codeck
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDeptEntity> implements SysDeptService {
    private final SysDeptMapper deptMapper;

    private final SysRoleMapper roleMapper;


    @Override
    public List<SysDeptEntity> listDept(SysDeptEntity dept) {
        LambdaQueryWrapper<SysDeptEntity> queryWrapper = Wrappers.lambdaQuery(SysDeptEntity.class)
                .eq(StrUtil.isNotBlank(dept.getStatus()), SysDeptEntity::getStatus, dept.getStatus())
                .like(StrUtil.isNotBlank(dept.getDeptName()), SysDeptEntity::getDeptName, dept.getDeptName())
                .orderByAsc(SysDeptEntity::getOrderNum);
        return deptMapper.selectDeptList(queryWrapper);
    }

    /**
     * 构建前端所需要树结构
     *
     * @param depts 部门列表
     * @return 树结构列表
     */
    @Override
    public List<SysDeptEntity> buildDeptTree(List<SysDeptEntity> depts) {
        List<SysDeptEntity> returnList = new ArrayList<>();
        List<Long> tempList = new ArrayList<>();
        for (SysDeptEntity dept : depts) {
            tempList.add(dept.getId());
        }
        for (Iterator<SysDeptEntity> iterator = depts.iterator(); iterator.hasNext(); ) {
            SysDeptEntity dept = iterator.next();
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(dept.getParentId())) {
                recursionFn(depts, dept);
                returnList.add(dept);
            }
        }
        if (returnList.isEmpty()) {
            returnList = depts;
        }
        return returnList;
    }

    /**
     * 构建前端所需要下拉树结构
     *
     * @param depts 部门列表
     * @return 下拉树结构列表
     */
    @Override
    public List<TreeSelectVO> buildDeptTreeSelect(List<SysDeptEntity> depts) {
        List<SysDeptEntity> deptTrees = buildDeptTree(depts);
        return deptTrees.stream().map(TreeSelectVO::new).collect(Collectors.toList());
    }

    /**
     * 根据角色ID查询部门树信息
     *
     * @param roleId 角色ID
     * @return 选中部门列表
     */
    @Override
    public List<Integer> getDeptListByRoleId(Long roleId) {
        SysRoleEntity role = roleMapper.selectById(roleId);
        return deptMapper.selectDeptListByRoleId(roleId, role.getDeptCheckStrictly());
    }


    /**
     * 根据ID查询所有子部门（正常状态）
     *
     * @param deptId 部门ID
     * @return 子部门数
     */
    @Override
    public int getNormalChildrenDeptById(Long deptId) {
        List<SysDeptEntity> allDeptList = CollUtil.newArrayList();
        List<SysDeptEntity> list = getChildrenDeptByIdList(CollUtil.newArrayList(deptId), allDeptList);
        if (CollectionUtil.isEmpty(list)) {
            return 0;
        }
        List<SysDeptEntity> sysDeptEntityList = list.stream().filter(item -> item.getStatus().equals("0")).collect(Collectors.toList());
        return CollectionUtil.isNotEmpty(sysDeptEntityList) ? sysDeptEntityList.size() : 0;
    }

    /**
     * 是否存在子节点
     *
     * @param deptId 部门ID
     * @return 结果
     */
    @Override
    public boolean hasChildByDeptId(Long deptId) {
        List<SysDeptEntity> idList = getChildrenDeptList(deptId);
        return CollectionUtil.isNotEmpty(idList);
    }


    /**
     * 获取子部门Id
     *
     * @param deptId
     * @return
     */
    public List<SysDeptEntity> getChildrenDeptList(Long deptId) {
        List<SysDeptEntity> allDeptList = CollUtil.newArrayList();
        return getChildrenDeptByIdList(CollUtil.newArrayList(deptId), allDeptList);
    }

    /**
     * 获取子部门Id
     *
     * @param deptId
     * @return
     */
    @Override
    public List<Long> getChildrenDeptIdList(Long deptId) {
        List<SysDeptEntity> list = getChildrenDeptList(deptId);
        if (CollectionUtil.isNotEmpty(list)) {
            return list.stream().map(BaseEntity::getId).collect(Collectors.toList());
        }
        return CollUtil.newArrayList();
    }


    /**
     * 递归查询子部门
     *
     * @param deptIdList  部门Id
     * @param allDeptList 空集合
     * @return
     */
    private List<SysDeptEntity> getChildrenDeptByIdList(List<Long> deptIdList, List<SysDeptEntity> allDeptList) {
        if (CollectionUtil.isEmpty(deptIdList)) {
            return CollUtil.newArrayList();
        }
        List<SysDeptEntity> list = deptMapper.selectList(Wrappers.<SysDeptEntity>lambdaQuery()
                .eq(SysDeptEntity::getDelFlag, "0")
                .in(SysDeptEntity::getParentId, deptIdList));
        if (CollectionUtil.isNotEmpty(list)) {
            allDeptList.addAll(list);
            getChildrenDeptByIdList(list.stream().map(BaseEntity::getId).collect(Collectors.toList()), allDeptList);
        }
        return allDeptList;
    }


    /**
     * 查询部门是否存在用户
     *
     * @param deptId 部门ID
     * @return 结果 true 存在 false 不存在
     */
    @Override
    public boolean checkDeptExistUser(Long deptId) {
        int result = deptMapper.checkDeptExistUser(deptId);
        return result > 0;
    }

    /**
     * 校验部门名称是否唯一
     *
     * @param dept 部门信息
     * @return 结果
     */
    @Override
    public String checkDeptNameUnique(SysDeptEntity dept) {
        Long deptId = ObjectUtil.isNull(dept.getId()) ? -1L : dept.getId();
        SysDeptEntity info = deptMapper.checkDeptNameUnique(dept.getDeptName(), dept.getParentId());
        if (ObjectUtil.isNotNull(info) && info.getId().longValue() != deptId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 新增保存部门信息
     *
     * @param dept 部门信息
     * @return 结果
     */
    @Override
    public int saveDept(SysDeptEntity dept) {
        SysDeptEntity info = deptMapper.selectById(dept.getParentId());
        // 如果父节点不为正常状态,则不允许新增子节点
        if (!UserConstants.DEPT_NORMAL.equals(info.getStatus())) {
            throw new BaseException("部门停用，不允许新增");
        }
        dept.setAncestors(info.getAncestors() + "," + dept.getParentId());
        return deptMapper.insert(dept);
    }

    /**
     * 修改保存部门信息
     *
     * @param dept 部门信息
     * @return 结果
     */
    @Override
    public int updateDept(SysDeptEntity dept) {
        SysDeptEntity newParentDept = deptMapper.selectById(dept.getParentId());
        SysDeptEntity oldDept = deptMapper.selectById(dept.getId());
        if (ObjectUtil.isNotNull(newParentDept) && ObjectUtil.isNotNull(oldDept)) {
            String newAncestors = newParentDept.getAncestors() + "," + newParentDept.getId();
            String oldAncestors = oldDept.getAncestors();
            dept.setAncestors(newAncestors);
            updateDeptChildren(dept.getId(), newAncestors, oldAncestors);
        }
        int result = deptMapper.updateById(dept);
        if (UserConstants.DEPT_NORMAL.equals(dept.getStatus())) {
            // 如果该部门是启用状态，则启用该部门的所有上级部门
            updateParentDeptStatusNormal(dept);
        }
        return result;
    }

    /**
     * 修改该部门的父级部门状态
     *
     * @param dept 当前部门
     */
    private void updateParentDeptStatusNormal(SysDeptEntity dept) {
        String ancestors = dept.getAncestors();
        List<Long> deptIds = Convert.toList(Long.class, ancestors);
        deptMapper.update(new SysDeptEntity() {{
            setStatus(SystemConstants.NORMAL_STATUS);
        }}, Wrappers.<SysDeptEntity>lambdaUpdate().in(SysDeptEntity::getId, deptIds));
    }

    /**
     * 修改子元素关系
     *
     * @param deptId       被修改的部门ID
     * @param newAncestors 新的父ID集合
     * @param oldAncestors 旧的父ID集合
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDeptChildren(Long deptId, String newAncestors, String oldAncestors) {
        List<SysDeptEntity> children = getChildrenDeptList(deptId);
        List<SysDeptEntity> list = new ArrayList<>();
        for (SysDeptEntity child : children) {
            SysDeptEntity dept = new SysDeptEntity();
            dept.setId(child.getId());
            dept.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
            list.add(dept);
        }
        if (!list.isEmpty()) {
            list.forEach(deptMapper::updateById);
        }
    }


    /**
     * 递归列表
     */
    private void recursionFn(List<SysDeptEntity> list, SysDeptEntity t) {
        // 得到子节点列表
        List<SysDeptEntity> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysDeptEntity tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<SysDeptEntity> getChildList(List<SysDeptEntity> list, SysDeptEntity t) {
        List<SysDeptEntity> tlist = new ArrayList<SysDeptEntity>();
        Iterator<SysDeptEntity> it = list.iterator();
        while (it.hasNext()) {
            SysDeptEntity n = it.next();
            if (ObjectUtil.isNotNull(n.getParentId()) && n.getParentId().longValue() == t.getId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<SysDeptEntity> list, SysDeptEntity t) {
        return getChildList(list, t).size() > 0;
    }
}
