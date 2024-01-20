/*
 ************************************
 * @项目名称: bhcard
 * @文件名称: JwtTokenFilter
 * @Date 2018/05/27
 * @Author will.zhao@bhex.io
 * @Copyright（C）: 2018 BlueHelix Inc.   All rights reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的。
 **************************************
 */
package io.bhex.bhop.common.jwt.filter;


import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import io.bhex.base.admin.AuthInfo;
import io.bhex.base.admin.ListAllAuthByUserIdReply;
import io.bhex.base.admin.common.AccountType;
import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.bhop.common.config.LocaleMessageService;
import io.bhex.bhop.common.dto.IpWhitelistDTO;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.grpc.client.AdminUserClient;
import io.bhex.bhop.common.jwt.authorize.Authorize;
import io.bhex.bhop.common.jwt.authorize.CookieProvider;
import io.bhex.bhop.common.jwt.authorize.JwtTokenProvider;
import io.bhex.bhop.common.service.AdminLoginUserService;
import io.bhex.bhop.common.service.AdminRoleAuthService;
import io.bhex.bhop.common.service.BaseCommonService;
import io.bhex.bhop.common.service.UserIpWhitelistService;
import io.bhex.bhop.common.util.PathPatternMatcher;
import io.bhex.bhop.common.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
//@Component
//@Order(1)
public class JwtTokenFilter implements Filter {

    @Autowired
    private Environment environment;
    @Autowired
    private LocaleMessageService localeMessageService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AdminUserClient adminUserClient;

    @Autowired
    private AdminRoleAuthService roleAuthService;

    @Autowired
    private AdminLoginUserService adminLoginUserService;

    @Autowired
    private UserIpWhitelistService userIpWhitelistService;

    @Resource
    private BaseCommonService baseCommonService;
    @Resource
    private CookieProvider cookieProvider;

    private List<String> ignoreSources;

    private List<String> gaIgnoreSources;

    private Boolean gaEnable;

    @PostConstruct
    public void init() {
        ignoreSources = this.ignoreResource(Authorize.IGNORE_RESOURCE);
        gaIgnoreSources = this.ignoreResource(Authorize.GA_IGNORE_RESOURCE);
        gaEnable = Boolean.valueOf(environment.getProperty(Authorize.GA_ENABLE));
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException {


        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setCharacterEncoding("UTF-8");

        try {

            Boolean newversion = environment.getProperty("authorize.newversion", Boolean.class);
            if (newversion != null && newversion) {
                chain.doFilter(request, response);
                return;
            }

            checkXssReferer(req);
            checkJwtToken(req, resp);
            chain.doFilter(request, response);
        } catch (BizException e) {
            log.warn("BizException  ", e);
            if (e.getCode() == ErrorCode.REPEATED_LOGIN_KICKED_OUT.getCode()) {
                adminLoginUserService.loginOut(resp, 0L);
            }
           String msg = localeMessageService.getMessage(e.getMessage());
            resp.getWriter().write("{\"code\":" + e.getCode()
                    + ",\"msg\":\"" + msg + "\"" + "}");
        } catch (Throwable t) {
            log.error("Unexpected error occurred in " + this.getClass().getName(), t);
            String msg = localeMessageService.getMessage("need.login");
            resp.getWriter().write("{\"code\":" + ErrorCode.LOGIN_TOKEN_ERROR.getCode()
                    + ",\"msg\":\"" + msg + "\"" + "}");
        }
    }

    private List<String> ignoreResource(String key) {
        String resources = environment.getProperty(key);
        return Splitter.on(",").splitToList(resources);
    }

    private void checkJwtToken(HttpServletRequest req, HttpServletResponse resp) {

        //坚持登录ip白名单
        checkLoginIp(req, resp);
        //判断 无需登录的链接
        Boolean ignore = ignoreSources.stream().anyMatch(resource -> req.getServletPath().startsWith(resource));
        Boolean gaIgnore = gaIgnoreSources.stream().anyMatch(resource -> req.getServletPath().startsWith(resource));
        if (ignore || gaIgnore) {
            return;
        }
        if (req.getServletPath().equals("/")
                && Strings.nullToEmpty(req.getHeader("user-agent")).contains("HealthChecker")) { //硬编码
            return;
        }
        //有些链接内网可以访问 10.* 172.* 192.* (限定内网访问的链接)
        //
//        if (req.getServletPath() in inertnalAllowPath && referer ip is internal) {
//            return;
//        }

        String jwtToken = RequestUtil.getCookieValue(req, Authorize.COOKIE_TOKEN);
        String subject = null;
        try {
            subject = jwtTokenProvider.parseSubject(jwtToken);
        } catch (BizException e) {
            throw e;
        }
        if (StringUtils.isEmpty(subject)) {
            log.info("subject is null. request path => {}.", req.getServletPath());
            throw new BizException(ErrorCode.LOGIN_TOKEN_ERROR, "token error");
        }
        Long userId = Long.valueOf(subject);
        if (userId == null) {
            throw new BizException(ErrorCode.LOGIN_TOKEN_ERROR, "need.login");
        }
        AdminUserReply user = adminUserClient.getAdminUserById(userId);
        if (null != user) {
            // 测试环境不开启ga验证
            if (gaEnable) {
                checkGA(user, req);
            }
            if (user.getStatus() == 2) {
                log.info("{} account be locked", userId);
                throw new BizException(ErrorCode.NO_PERMISSION, "account.be.locked");
            }
            //root用户可以访问全部链接
            if (user.getAccountType() == AccountType.ROOT_ACCOUNT) {
                return;
            } else {
                //判断登录用户是否有此路径的访问权限
                //获取当前用户的全部权限路径进行匹配
                ListAllAuthByUserIdReply allAuthReply = roleAuthService.listAllAuthByUserId(user.getOrgId(), user.getId());
                String path = "";
                for (AuthInfo authInfo: allAuthReply.getAuthPathInfosList()) {
                    if (pathMatch(req.getServletPath(), authInfo.getPath())) {
                        return;
                    }
                    path += authInfo.getPath() + ",";
                }
                log.warn("no permission:{} {}", req.getServletPath(), path);
                // 所有权限都不匹配，则提示权限异常，不可访问
                throw new BizException(ErrorCode.NO_PERMISSION, ErrorCode.NO_PERMISSION.getDesc());
            }
        } else {
            throw new BizException(ErrorCode.LOGIN_TOKEN_ERROR, "need.login");
        }
    }

    private void checkLoginIp(HttpServletRequest req, HttpServletResponse resp) {
        //log.info("check login ip: requestUrl {}", req.getServletPath());
        Long orgId = baseCommonService.getOrgId(req);
        String realIP = RequestUtil.getRealIP(req);
        //log.info("check login ip: orgId {}", orgId);
        if (orgId == null || orgId == 0) {
            //如果获取不到orgid，用ip直接访问的，如 /healty/check 或者 其它从saas过来的内部接口
            return;
        }
        List<IpWhitelistDTO> dtoList = userIpWhitelistService.showIpWhitelist(orgId);
        if (!CollectionUtils.isEmpty(dtoList)) {
            for (IpWhitelistDTO ipWhitelistDTO: dtoList) {
                if (realIP.equals(ipWhitelistDTO.getIpAddress())) {
                    return;
                }
            }
            loginOut(resp);
            log.warn("Admin login error: login ip not in whitelist. IP={}, OrgId={}", RequestUtil.getRealIP(req), orgId);
            throw new BizException(ErrorCode.IP_NOT_IN_WHITELIST_ERROR);
        }
    }

    private void loginOut(HttpServletResponse response) {
        cookieProvider.clear(response, Authorize.COOKIE_TOKEN);
        cookieProvider.clear(response, Authorize.LOGIN_COOKIE);
        cookieProvider.clear(response, Authorize.ATTRIBUTE_USER_ID);
    }

    // 检查是否绑定GA、手机。如果未绑定，则必须绑定才可访问其他功能
    private Boolean checkGA(AdminUserReply user, HttpServletRequest req) {
        // 未绑定GA 只可以访问特定链接，剩下的都拒绝
        if (!user.getBindGa() && !user.getBindPhone()) {
            //判断 无需登录的链接
            boolean ignore = gaIgnoreSources.stream().anyMatch(resource -> req.getServletPath().startsWith(resource));
            if (ignore) {
                return ignore;
            }
            throw new BizException(ErrorCode.UNBIND_GA_ERROR, ErrorCode.UNBIND_GA_ERROR.getDesc());
        } else {
            return true;
        }
    }

    private void checkXssReferer(HttpServletRequest req) {
        String refererDomain = Optional.ofNullable(environment.getProperty("authorize.referer.domain"))
                .orElse("");
        Boolean refererCheck = Optional.ofNullable(environment.getProperty("authorize.referer.check", Boolean.class))
                .orElse(true);
        String refererFromHeader = req.getHeader("Referer");

        if (refererCheck && !refererFromHeader.startsWith(refererDomain)) {
            log.warn("xss request. Base={}, Referer={}, IP={}", refererDomain, refererFromHeader, RequestUtil.getRealIP(req));
            throw new BizException(ErrorCode.LOGIN_TOKEN_ERROR, "token error");
        }
    }

    @Override
    public void destroy() {
    }

    /**
     * 匹配路径是否在控制域的范围内
     *
     * @param path
     * @param domain
     * @return
     */
    private static boolean pathMatch(String path, String domain) {
        if (PathPatternMatcher.isPattern(domain)) {
            return PathPatternMatcher.match(domain, path);
        } else {
            return domain.equals(path);
        }
    }

}


