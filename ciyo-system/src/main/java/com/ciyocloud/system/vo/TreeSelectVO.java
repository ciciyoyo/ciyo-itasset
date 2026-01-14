package com.ciyocloud.system.vo;

import com.ciyocloud.system.entity.SysDeptEntity;
import com.ciyocloud.system.entity.SysMenuEntity;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Treeselect树结构实体类
 *
 * @author codeck
 */
public class TreeSelectVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 节点ID
     */
    private Long id;

    /**
     * 节点名称
     */
    private String label;

    /**
     * 子节点
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TreeSelectVO> children;

    public TreeSelectVO() {

    }

    public TreeSelectVO(SysDeptEntity dept) {
        this.id = dept.getId();
        this.label = dept.getDeptName();
        this.children = dept.getChildren().stream().map(TreeSelectVO::new).collect(Collectors.toList());
    }

    public TreeSelectVO(SysMenuEntity menu) {
        this.id = menu.getId();
        this.label = menu.getMenuName();
        this.children = menu.getChildren().stream().map(TreeSelectVO::new).collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<TreeSelectVO> getChildren() {
        return children;
    }

    public void setChildren(List<TreeSelectVO> children) {
        this.children = children;
    }
}
