package io.bhex.bhop.common.dto.param;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import java.io.Serializable;
import java.util.List;

@Data
public class ShowSubUserPO {

    private Long adminId;

    private Long orgId;

}
