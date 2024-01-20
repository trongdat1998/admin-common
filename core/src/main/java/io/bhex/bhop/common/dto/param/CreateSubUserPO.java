package io.bhex.bhop.common.dto.param;

import io.bhex.bhop.common.util.validation.CommonInputValid;
import io.bhex.bhop.common.util.validation.PhoneValid;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class CreateSubUserPO implements Serializable {

    @NotBlank
    @CommonInputValid
    private String username;

    @CommonInputValid
    private String nationalCode;

    @PhoneValid(allowEmpty = true)
    private String telephone;

    //private String password;

    @NotEmpty
    @Email(regexp="(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")
    private String email;

    private Long orgId;

//    private String defaultLanguage;
//
//    CreateUserType createUserType;

    @CommonInputValid
    private String position;

    private List<Long> roleIds;

    private Integer authType;
    private String verifyCode;

}
