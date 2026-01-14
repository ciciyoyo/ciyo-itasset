package com.ciyocloud.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.SysBaseEntity;
import lombok.Data;

/**
 * 用户和岗位关联 sys_user_post
 *
 * @author codeck
 */
@Data
@TableName("sys_user_post")
public class SysUserPostEntity extends SysBaseEntity {


    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 岗位ID
     */
    private Long postId;


}
