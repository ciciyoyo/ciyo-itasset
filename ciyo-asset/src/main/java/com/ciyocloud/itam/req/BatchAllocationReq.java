package com.ciyocloud.itam.req;

import com.ciyocloud.itam.enums.AllocationOwnerType;
import com.ciyocloud.itam.enums.AssetType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BatchAllocationReq {

    @NotNull(message = "资源类型不能为空")
    private AssetType itemType;

    @NotNull(message = "资源ID不能为空")
    private Long itemId;

    @NotNull(message = "归属类型不能为空")
    private AllocationOwnerType ownerType;

    @NotEmpty(message = "归属ID列表不能为空")
    private List<Long> ownerIds;

    private String note;
}
