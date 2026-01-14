package com.ciyocloud.common.entity.request;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * @author : smalljop
 * @description : 分页
 * @create : 2020-12-09 10:47
 **/
@Data
public class PageRequest {


    /**
     * 当前页 从1开始
     */
    private int current = 1;
    /**
     * 每页大小 默认20
     */
    private int size = 20;


    public int getOffsetNum() {
        return (current - 1) * size;
    }

    public <T> Page<T> toMpPage() {
        return new Page<>(current, size);
    }
}
