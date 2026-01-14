package com.ciyocloud.storage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ciyocloud.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 系统文件表实体类
 *
 * @author ciyo
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("sys_file")
public class SysFileEntity extends BaseEntity<SysFileEntity> {

    private static final long serialVersionUID = 1L;

    /**
     * 原始文件名
     */
    private String originName;

    /**
     * 存储文件名（uuid）
     */
    private String fileName;

    /**
     * 文件扩展名
     */
    private String fileExt;

    /**
     * MIME 类型
     */
    private String contentType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 物理存储路径（用于删除）
     */
    private String filePath;

    /**
     * 访问 URL
     */
    private String fileUrl;

    /**
     * 业务类型，如 ASSET / MANUFACTURER
     */
    private String bizType;

    /**
     * 业务ID
     */
    private String bizId;

    /**
     * 业务字段，如 logo / attachment
     */
    private String bizField;

    /**
     * 是否临时文件 1是 0否
     */
    private Integer isTemp;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 状态 1正常 2待删除 3已删除
     */
    private Integer status;

}
