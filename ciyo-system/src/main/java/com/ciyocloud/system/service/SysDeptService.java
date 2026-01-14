package com.ciyocloud.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ciyocloud.system.entity.SysDeptEntity;
import com.ciyocloud.system.vo.TreeSelectVO;

import java.util.List;

/**
 * 部门管理 服务层
 *
 * @author codeck
 */
public interface SysDeptService extends IService<SysDeptEntity> {


    /**
     * 查询部门管理数据
     */
    List<SysDeptEntity> listDept(SysDeptEntity dept);

    /**
     * 构建前端所需要树结构
     *
     * @param depts 部门列表
     * @return 树结构列表
     */
    List<SysDeptEntity> buildDeptTree(List<SysDeptEntity> depts);

    /**
     * 构建前端所需要下拉树结构
     *
     * @param depts 部门列表
     * @return 下拉树结构列表
     */
    List<TreeSelectVO> buildDeptTreeSelect(List<SysDeptEntity> depts);

    /**
     * 根据角色ID查询部门树信息
     *
     * @param roleId 角色ID
     * @return 选中部门列表
     */
    List<Integer> getDeptListByRoleId(Long roleId);


    /**
     * 根据ID查询所有子部门（正常状态）
     *
     * @param deptId 部门ID
     * @return 子部门数
     */
    int getNormalChildrenDeptById(Long deptId);

    /**
     * 是否存在部门子节点
     *
     * @param deptId 部门ID
     */
    boolean hasChildByDeptId(Long deptId);

    /**
     * 获取子部门Id
     *
     * @param deptId 部门Id
     * @return 子部门Id
     */
    List<Long> getChildrenDeptIdList(Long deptId);

    /**
     * 查询部门是否存在用户
     *
     * @param deptId 部门ID
     * @return 结果 true 存在 false 不存在
     */
    boolean checkDeptExistUser(Long deptId);

    /**
     * 校验部门名称是否唯一
     *
     * @param dept 部门信息
     * @return 结果
     */
    String checkDeptNameUnique(SysDeptEntity dept);

    /**
     * 保存部门
     *
     * @param dept
     * @return
     */
    int saveDept(SysDeptEntity dept);

    /**
     * 修改部门
     *
     * @param dept
     * @return
     */
    int updateDept(SysDeptEntity dept);


}
