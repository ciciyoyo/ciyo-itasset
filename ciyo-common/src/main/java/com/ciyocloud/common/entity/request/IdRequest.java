package com.ciyocloud.common.entity.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * id 请求
 *
 * @author : codeck
 * @since :  2023/09/08 21:52
 **/
@Data
public class IdRequest {

    /**
     * id
     */
    @NotBlank(message = "{sys.id.notnull}")
    private Long id;
}
