package com.ciyocloud.system.vo;

import com.ciyocloud.system.entity.SysDeptPostEntity;
import lombok.Data;

/**
 * @author : codeck
 **/
@Data
public class SysDeptPostVO extends SysDeptPostEntity {

    /**
     * 岗位名称
     */
    private String postName;

    /**
     * 岗位排序
     */
    private String level;

    /**
     * 领导岗位
     */
    private Integer leaderPost;

}
