package io.bhex.bhop.common.dto.param;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class EnableSubUserPO implements Serializable {

    private Long adminId;
    private Long orgId;
    private Integer status;
}
