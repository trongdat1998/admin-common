package io.bhex.bhop.common.service;

import io.bhex.bhop.common.entity.AdminUser;
import lombok.Builder;
import lombok.Data;

public interface OpenapiService {

    @Data
    @Builder
    class OpenapiAuthenticateResult {
        Integer result;
        AdminUser user;
    }

    OpenapiAuthenticateResult openapiAuthenticate(String accessKey, String originalRequestStr, String signature);

}
