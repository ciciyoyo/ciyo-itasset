package com.ciyocloud.system.vo;

import com.ciyocloud.system.entity.SysAnnounceEntity;
import lombok.Data;

import java.util.List;

/**
 * 系统通告VO
 *
 * @author codeck
 */
@Data
public class SysAnnounceVO extends SysAnnounceEntity {


    /**
     * 指定用户列表
     */
    private List<SysUserVO> userList;

}
