package io.bhex.bhop.common.service.impl;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import io.bhex.bhop.common.entity.AdminApiKey;
import io.bhex.bhop.common.entity.AdminUser;
import io.bhex.bhop.common.mapper.AdminApiKeyMapper;
import io.bhex.bhop.common.mapper.AdminUserMapper;
import io.bhex.bhop.common.service.OpenapiService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("openapiService")
public class OpenapiServiceImpl implements OpenapiService {

    @Resource
    private AdminApiKeyMapper adminApiKeyMapper;

    @Resource
    private AdminUserMapper adminUserMapper;

    @Override
    public OpenapiAuthenticateResult openapiAuthenticate(String accessKey, String originalRequestStr, String signature) {
        AdminApiKey apiKey = adminApiKeyMapper.getByAccessKey(accessKey);
        if (apiKey == null) {
            return OpenapiAuthenticateResult.builder().result(-1).build();
        }
        if (apiKey.getStatus() == 0) {
            return OpenapiAuthenticateResult.builder().result(-2).build();
        }
        String secretKey = apiKey.getSecretKey();
        if (!Hashing.hmacSha256(secretKey.getBytes()).hashString(originalRequestStr, Charsets.UTF_8).toString().equals(signature)) {
            return OpenapiAuthenticateResult.builder().result(-3).build();
        }
        Long orgId = apiKey.getOrgId();
        Long userId = apiKey.getUserId();
        AdminUser adminUser = adminUserMapper.selectAdminUserByIdAndOrgId(userId, orgId);
        if (adminUser.getStatus() != AdminUser.ENABLE_STATUS.intValue()) {
            return OpenapiAuthenticateResult.builder().result(-2).build();
        }
        return OpenapiAuthenticateResult.builder().result(0).user(adminUser).build();
    }

}
