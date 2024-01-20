package io.bhex.bhop.common.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class GeetestV3RegisterDTO {
    private Integer success;
    @SerializedName("new_captcha")
    private Boolean newCaptcha;
    private String challenge;
    private String gt;
}