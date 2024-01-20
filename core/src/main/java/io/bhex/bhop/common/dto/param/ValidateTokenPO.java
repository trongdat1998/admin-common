package io.bhex.bhop.common.dto.param;

import lombok.Data;

@Data
public class ValidateTokenPO {

    private Long adminUserId;

    private String token;
}
