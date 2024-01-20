package io.bhex.bhop.common.service;

import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;
import io.bhex.bhop.common.config.OrgInstanceConfig;
import io.bhex.bhop.common.enums.AdminPlatformEnum;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.service
 * @Author: ming.xu
 * @CreateDate: 2019/3/21 11:45 AM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Service
public class BaseCommonService {

    @Resource
    private Environment environment;

    @Resource
    private OrgInstanceConfig orgInstanceConfig;

    public AdminPlatformEnum getAdminPlatform() {
        String serverName = environment.getProperty("spring.application.name", String.class);
        return AdminPlatformEnum.getByServerName(serverName);
    }

    public Long getOrgId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return getOrgId(request);
    }

    public Long getOrgId(HttpServletRequest request) {
        AdminPlatformEnum platform = getAdminPlatform();
        if (platform.equals(AdminPlatformEnum.SAAS_ADMIN_PLATFROM)) {
            //saas平台没有org的概念
            return 0L;
        }
        String httpServerName = request.getServerName();
        if (platform.equals(AdminPlatformEnum.BROKER_ADMIN_PLATFROM)) {
            return orgInstanceConfig.getBrokerIdByDomain(httpServerName);
        }
        if (platform.equals(AdminPlatformEnum.EXCHANGE_ADMIN_PLATFROM)) {
            return orgInstanceConfig.getExchangeIdByDomain(httpServerName);
        }
        return null;
    }

    public String getRemoteIp(HttpServletRequest request) {
        String ip;
        ip = request.getHeader("X-Real-IP");
        if (!Strings.isNullOrEmpty(ip) && InetAddresses.isInetAddress(ip)) {
            return ip;
        }
        ip = request.getHeader("X-Forwarded-For");
        if (!Strings.isNullOrEmpty(ip) && InetAddresses.isInetAddress(ip)) {
            return ip;
        }
        ip = request.getHeader("Proxy-Client-IP");
        if (!Strings.isNullOrEmpty(ip) && InetAddresses.isInetAddress(ip)) {
            return ip;
        }
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (!Strings.isNullOrEmpty(ip) && InetAddresses.isInetAddress(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    public String getSaasAdminUrl() {
        String adminUrl = environment.getProperty("saas.admin.url", String.class);
        return adminUrl == null ? "https://saas.bhop.cloud/" : adminUrl;
    }
}
