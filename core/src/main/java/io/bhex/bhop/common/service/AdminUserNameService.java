package io.bhex.bhop.common.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.bhop.common.grpc.client.AdminUserClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class AdminUserNameService {

    @Resource
    private AdminUserClient adminUserClient;

    private Cache<String, String> adminUserCache = CacheBuilder
            .newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(300L, TimeUnit.SECONDS)
            .build();

    public String getAdminName(long orgId, String email) {
        try {
            return adminUserCache.get(orgId+email, ()-> {
                AdminUserReply adminUser = adminUserClient.getAdminUserByEmail(email, orgId);
                return adminUser.getRealName();
            });
        } catch (ExecutionException e) {
            return "";
        }
    }
}
