package com.ciyocloud.system.request;

import com.ciyocloud.common.entity.request.IdListRequest;
import lombok.Data;

/**
 * @author : codeck
 * @description :
 * @create :  2022/06/07 13:31
 **/
@Data
public class DeptPostRequest {

    @Data
    public static class Setting extends IdListRequest {

        private Long deptId;

    }

}
