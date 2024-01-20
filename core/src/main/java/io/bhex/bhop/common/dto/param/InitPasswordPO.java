package io.bhex.bhop.common.dto.param;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class InitPasswordPO {

    @NotEmpty
    private String password;

    //@NotEmpty
    //private String confirmedPassword;

    @NotEmpty
    private String token;

    @NotNull
    private Long adminUserId;
}
