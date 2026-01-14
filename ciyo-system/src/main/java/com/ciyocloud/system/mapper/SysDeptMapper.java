package com.ciyocloud.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.ciyocloud.datapermission.annotation.DataColumn;
import com.ciyocloud.datapermission.annotation.DataPermission;
import com.ciyocloud.system.entity.SysDeptEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * 部门管理 数据层
 *
 * @author codeck
 */
public interface SysDeptMapper extends BaseMapper<SysDeptEntity> {


    /**
     * 根据角色ID查询部门树信息
     *
     * @param roleId            角色ID
     * @param deptCheckStrictly 部门树选择项是否关联显示
     * @return 选中部门列表
     */
    List<Integer> selectDeptListByRoleId(@Param("roleId") Long roleId, @Param("deptCheckStrictly") boolean deptCheckStrictly);


    /**
     * 查询部门是否存在用户
     *
     * @param deptId 部门ID
     * @return 结果
     */
    int checkDeptExistUser(Long deptId);

    /**
     * 校验部门名称是否唯一
     *
     * @param deptName 部门名称
     * @param parentId 父部门ID
     * @return 结果
     */
    SysDeptEntity checkDeptNameUnique(@Param("deptName") String deptName, @Param("parentId") Long parentId);


    /**
     * 修改子元素关系
     *
     * @param depts 子元素
     * @return 结果
     */
    int updateDeptChildren(@Param("depts") List<SysDeptEntity> depts);


    @DataPermission({
            @DataColumn(key = "deptName", value = "id")
    })
    @Select("select * from sys_dept ${ew.getCustomSqlSegment}")
    List<SysDeptEntity> selectDeptList(@Param(Constants.WRAPPER) Wrapper<SysDeptEntity> queryWrapper);
}
