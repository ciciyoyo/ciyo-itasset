package com.ciyocloud.system.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ciyocloud.system.constant.UserConstants;
import com.ciyocloud.system.entity.SysMenuEntity;
import com.ciyocloud.system.entity.SysRoleEntity;
import com.ciyocloud.system.entity.SysRoleMenuEntity;
import com.ciyocloud.system.entity.SysUserEntity;
import com.ciyocloud.system.mapper.SysMenuMapper;
import com.ciyocloud.system.mapper.SysRoleMapper;
import com.ciyocloud.system.mapper.SysRoleMenuMapper;
import com.ciyocloud.system.service.SysMenuService;
import com.ciyocloud.system.vo.MetaVO;
import com.ciyocloud.system.vo.RouterVO;
import com.ciyocloud.system.vo.TreeSelectVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单 业务层处理
 *
 * @author codeck
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuEntity> implements SysMenuService {

    private final SysMenuMapper menuMapper;

    private final SysRoleMapper roleMapper;

    private final SysRoleMenuMapper roleMenuMapper;

    /**
     * 根据用户查询系统菜单列表
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Override
    public List<SysMenuEntity> getMenuList(Long userId) {
        return getMenuList(new SysMenuEntity(), userId);
    }

    /**
     * 查询系统菜单列表
     *
     * @param menu 菜单信息
     * @return 菜单列表
     */
    @Override
    public List<SysMenuEntity> getMenuList(SysMenuEntity menu, Long userId) {
        List<SysMenuEntity> menuList = null;
        // 管理员显示所有菜单信息
        if (SysUserEntity.isAdmin(userId)) {
            LambdaQueryWrapper<SysMenuEntity> queryWrapper = Wrappers.<SysMenuEntity>lambdaQuery()
                    .eq(StrUtil.isNotBlank(menu.getStatus()), SysMenuEntity::getStatus, menu.getStatus())
                    .like(StrUtil.isNotBlank(menu.getMenuName()), SysMenuEntity::getMenuName, menu.getMenuName())
                    .orderByAsc(SysMenuEntity::getParentId, SysMenuEntity::getOrderNum);
            menuList = menuMapper.selectList(queryWrapper);
        } else {
            QueryWrapper<SysMenuEntity> wrapper = Wrappers.query();
            wrapper.like(StrUtil.isNotBlank(menu.getMenuName()), "m.menu_name", menu.getMenuName())
                    .eq("sur.user_id", userId)
                    .eq(StrUtil.isNotBlank(menu.getVisible()), "m.visible", menu.getVisible())
                    .eq(StrUtil.isNotBlank(menu.getStatus()), "m.status", menu.getStatus())
                    .orderByAsc("m.parent_id", "m.order_num");
            menuList = menuMapper.selectMenuListByUserId(wrapper);
        }
        return menuList;
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> getMenuPermsByUserId(Long userId) {
        List<String> perms = menuMapper.selectMenuPermsByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (String perm : perms) {
            if (StringUtils.isNotEmpty(perm)) {
                permsSet.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 根据用户ID查询菜单
     *
     * @param userId 用户名称
     * @return 菜单列表
     */
    @Override
    public List<SysMenuEntity> getMenuTreeByUserId(Long userId) {
        List<SysMenuEntity> menus = null;
        if (SysUserEntity.isAdmin(userId)) {
            menus = menuMapper.selectMenuTreeAll();
        } else {
            menus = menuMapper.selectMenuTreeByUserId(userId);
        }
        return getChildPerms(menus, 0);
    }

    /**
     * 获取菜单树信息
     *
     * @return 选中菜单列表
     */
    @Override
    public List<SysMenuEntity> getMenuTreeAll() {
        List<SysMenuEntity> menus = menuMapper.selectMenuTreeAll();
        return getChildPerms(menus, 0);
    }

    /**
     * 根据角色ID查询菜单树信息
     *
     * @param roleId 角色ID
     * @return 选中菜单列表
     */
    @Override
    public List<Integer> getMenuListByRoleId(Long roleId) {
        SysRoleEntity role = roleMapper.selectById(roleId);
        return menuMapper.selectMenuListByRoleId(roleId, role.getMenuCheckStrictly());
    }

    /**
     * 构建前端路由所需要的菜单
     *
     * @param menus 菜单列表
     * @return 路由列表
     */
    @Override
    public List<RouterVO> buildMenus(List<SysMenuEntity> menus) {
        List<RouterVO> routers = new LinkedList<RouterVO>();
        for (SysMenuEntity menu : menus) {
            RouterVO router = new RouterVO();
            router.setName(getRouteName(menu));
            router.setPath(getRouterPath(menu));
            router.setComponent(getComponent(menu));
            router.setMeta(new MetaVO(menu));
            List<SysMenuEntity> cMenus = menu.getChildren();
            // 有子菜单 并且是目录
            if (CollUtil.isNotEmpty(cMenus) && UserConstants.TYPE_DIR.equals(menu.getMenuType())) {
                SysMenuEntity firstChildMenu = CollUtil.getFirst(cMenus);
                if (ObjectUtil.isNull(firstChildMenu)) {
                    router.setRedirect(getRouterPath(firstChildMenu));
                }
                router.setChildren(buildMenus(cMenus));
            }
            routers.add(router);
        }
        return routers;
    }


    @Override
    public List<SysMenuEntity> buildMenusEntity(List<SysMenuEntity> menus) {
        List<SysMenuEntity> routers = new LinkedList<SysMenuEntity>();
        for (SysMenuEntity menu : menus) {
            List<SysMenuEntity> cMenus = menu.getChildren();
            if (cMenus != null && !cMenus.isEmpty() && UserConstants.TYPE_DIR.equals(menu.getMenuType())) {
                menu.setChildren(buildMenusEntity(cMenus));
            }
            if (cMenus != null && menu.getChildren().isEmpty()) {
                menu.setChildren(null);
            }
            routers.add(menu);
        }
        return routers;
    }

    /**
     * 构建前端所需要树结构
     *
     * @param menus 菜单列表
     * @return 树结构列表
     */
    @Override
    public List<SysMenuEntity> buildMenuTree(List<SysMenuEntity> menus) {
        List<SysMenuEntity> returnList = new ArrayList<SysMenuEntity>();
        List<Long> tempList = new ArrayList<Long>();
        for (SysMenuEntity dept : menus) {
            tempList.add(dept.getId());
        }
        for (Iterator<SysMenuEntity> iterator = menus.iterator(); iterator.hasNext(); ) {
            SysMenuEntity menu = iterator.next();
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(menu.getParentId())) {
                recursionFn(menus, menu);
                returnList.add(menu);
            }
        }
        if (returnList.isEmpty()) {
            returnList = menus;
        }
        return returnList;
    }

    /**
     * 构建前端所需要下拉树结构
     *
     * @param menus 菜单列表
     * @return 下拉树结构列表
     */
    @Override
    public List<TreeSelectVO> buildMenuTreeSelect(List<SysMenuEntity> menus) {
        List<SysMenuEntity> menuTrees = buildMenuTree(menus);
        return menuTrees.stream().map(TreeSelectVO::new).collect(Collectors.toList());
    }


    /**
     * 是否存在菜单子节点
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public boolean hasChildByMenuId(Long menuId) {
        long result = menuMapper.selectCount(Wrappers.<SysMenuEntity>lambdaQuery().eq(SysMenuEntity::getParentId, menuId));
        return result > 0;
    }

    /**
     * 查询菜单使用数量
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public boolean checkMenuExistRole(Long menuId) {
        long result = roleMenuMapper.selectCount(Wrappers.<SysRoleMenuEntity>lambdaQuery().eq(SysRoleMenuEntity::getMenuId, menuId));
        return result > 0;
    }

    /**
     * 校验菜单名称是否唯一
     *
     * @param menu 菜单信息
     * @return 结果
     */
    @Override
    public String checkMenuNameUnique(SysMenuEntity menu) {
        Long menuId = ObjectUtil.isNull(menu.getId()) ? -1L : menu.getId();
        SysMenuEntity info = menuMapper.selectOne(Wrappers.<SysMenuEntity>lambdaQuery().eq(SysMenuEntity::getMenuName, menu.getMenuName()).eq(SysMenuEntity::getParentId, menu.getParentId()));
        if (ObjectUtil.isNotNull(info) && info.getId().longValue() != menuId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 获取路由名称
     *
     * @param menu 菜单信息
     * @return 路由名称
     */
    public String getRouteName(SysMenuEntity menu) {
        if (StrUtil.isBlank(menu.getPath())) {
            return StringUtils.EMPTY;
        }
        // 移除getPath所有的符号
        // 如果是/ 开头
        String routerName = "";
        if (menu.getPath().startsWith("/")) {
            // 根据/转首字母大写 比如/sys/user 转为 SysUser
            routerName = toCamelCase(menu.getPath());
        } else {
            routerName = StringUtils.capitalize(menu.getPath()).replaceAll("[^a-zA-Z0-9]", "");
        }
        // 非外链并且是一级目录（类型为目录）
//        if (isNotMenuFrame(menu)) {
//            routerName = StringUtils.EMPTY;
//        }
        return routerName;
    }


    private String toCamelCase(String str) {
        // 如果首字母是/ 就移除掉
        if (str.startsWith("/")) {
            str = str.substring(1);
        }
        return StrUtil.upperFirst(StrUtil.toCamelCase(str.replaceAll("/", "_")));
    }


    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    public String getRouterPath(SysMenuEntity menu) {
        String routerPath = menu.getPath();
        // 外链
        if (Validator.isUrl(menu.getPath())) {
            routerPath = "/link" + menu.getId();
        } else {
            // 如果前面不包含/ 则加上
//            if (!routerPath.startsWith("/")) {
//                routerPath = "/" + routerPath;
//            }
        }
        return routerPath;
    }

    /**
     * 获取组件信息
     *
     * @param menu 菜单信息
     * @return 组件信息
     */
    public String getComponent(SysMenuEntity menu) {
        String component = UserConstants.PARENT_VIEW;
        // 菜单目录
        if (isParentView(menu)) {
            // 是
            component = UserConstants.PARENT_VIEW;
        }
        // 外链菜单
        if (Validator.isUrl(menu.getPath())) {
            if (menu.getIsFrame().equals(UserConstants.YES_FRAME)) {
                component = UserConstants.IFRAME_VIEW;
            } else {
                component = UserConstants.LINK_VIEW;
            }
        } else {
            component = menu.getComponent();
        }
        return component;
    }

    /**
     * 是菜单并且不是外链
     *
     * @param menu 菜单信息
     * @return 结果
     */
    public boolean isNotMenuFrame(SysMenuEntity menu) {
        return menu.getParentId().intValue() == 0 && UserConstants.TYPE_MENU.equals(menu.getMenuType()) &&
                menu.getIsFrame().equals(UserConstants.NO_FRAME);
    }

    /**
     * 是否为parent_view组件
     *
     * @param menu 菜单信息
     * @return 结果
     */
    public boolean isParentView(SysMenuEntity menu) {
        return menu.getParentId().intValue() != 0 && UserConstants.TYPE_DIR.equals(menu.getMenuType());
    }

    /**
     * 根据父节点的ID获取所有子节点
     *
     * @param list     分类表
     * @param parentId 传入的父节点ID
     * @return String
     */
    public List<SysMenuEntity> getChildPerms(List<SysMenuEntity> list, int parentId) {
        List<SysMenuEntity> returnList = new ArrayList<SysMenuEntity>();
        for (Iterator<SysMenuEntity> iterator = list.iterator(); iterator.hasNext(); ) {
            SysMenuEntity t = iterator.next();
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.getParentId() == parentId) {
                recursionFn(list, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    /**
     * 递归列表
     *
     * @param list
     * @param t
     */
    private void recursionFn(List<SysMenuEntity> list, SysMenuEntity t) {
        // 得到子节点列表
        List<SysMenuEntity> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysMenuEntity tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<SysMenuEntity> getChildList(List<SysMenuEntity> list, SysMenuEntity t) {
        List<SysMenuEntity> tlist = new ArrayList<SysMenuEntity>();
        Iterator<SysMenuEntity> it = list.iterator();
        while (it.hasNext()) {
            SysMenuEntity n = it.next();
            if (n.getParentId().longValue() == t.getId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<SysMenuEntity> list, SysMenuEntity t) {
        return getChildList(list, t).size() > 0;
    }
}
