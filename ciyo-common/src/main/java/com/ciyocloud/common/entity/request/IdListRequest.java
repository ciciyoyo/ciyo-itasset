package com.ciyocloud.common.entity.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author : codeck
 * @description : ID列表请求
 * @create :  2022/06/07 13:20
 **/
@Data
public class IdListRequest {

    /**
     * 需要删除的Id
     */
    @NotNull
    private List<Long> ids;

    /**
     * 扩展的值 比如用来传一些特殊的值 非必填
     */
    private String key;
}
