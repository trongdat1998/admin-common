package io.bhex.bhop.common.jwt.filter;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.bhex.base.admin.AuthInfo;
import io.bhex.base.admin.ListAllAuthByUserIdReply;
import io.bhex.base.admin.common.AccountType;
import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.bhop.common.dto.IpWhitelistDTO;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.grpc.client.AdminUserClient;
import io.bhex.bhop.common.jwt.authorize.Authorize;
import io.bhex.bhop.common.jwt.authorize.JwtTokenProvider;
import io.bhex.bhop.common.service.AdminLoginUserService;
import io.bhex.bhop.common.service.AdminRoleAuthService;
import io.bhex.bhop.common.service.BaseCommonService;
import io.bhex.bhop.common.service.UserIpWhitelistService;
import io.bhex.bhop.common.util.PathPatternMatcher;
import io.bhex.bhop.common.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AccessAuthorizeInterceptor implements HandlerInterceptor, InitializingBean {

    @Autowired
    private Environment environment;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AdminUserClient adminUserClient;

    @Autowired
    private AdminRoleAuthService roleAuthService;

    @Autowired
    private UserIpWhitelistService userIpWhitelistService;

    @Resource
    private BaseCommonService baseCommonService;

    @Autowired
    private AdminLoginUserService adminLoginUserService;

    private Boolean gaEnable;

    @PostConstruct
    public void init() {
        gaEnable = Boolean.valueOf(environment.getProperty(Authorize.GA_ENABLE));
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {

        

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        if (req.getServletPath().equals("/")
                && Strings.nullToEmpty(req.getHeader("user-agent")).contains("HealthChecker")) { //硬编码
            return true;
        }

        boolean internal = false;
        boolean verifyLogin = true;
        boolean verifyAuth = true;
        boolean verifyGaOrPhone = true;
        long[] authIds = {};

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        AccessAnnotation accessAnnotation = handlerMethod.getMethodAnnotation(AccessAnnotation.class);
        if (accessAnnotation == null) {
            accessAnnotation = handlerMethod.getBeanType().getAnnotation(AccessAnnotation.class);
        }
        if (accessAnnotation != null) {
            internal = accessAnnotation.internal();
            verifyLogin = accessAnnotation.verifyLogin();
            verifyAuth = accessAnnotation.verifyAuth();
            verifyGaOrPhone = accessAnnotation.verifyGaOrPhone();
            authIds = accessAnnotation.authIds();
        }

        if (internal) {
            int port = req.getServerPort();
            int serverPort = environment.getProperty("server.port", Integer.class);
            if (port != serverPort) {
                log.error("external user wanna access internal resource!!!!");
                resp.setStatus(403);
                return false;
            }
            return true;
            //String adminRequest = Strings.nullToEmpty(req.getHeader("AdminRequest"));
            //return !StringUtils.isEmpty(adminRequest);
        }

        // 检查登录ip白名单
        checkLoginIp(req, resp);

        //verify login
        if (!verifyLogin) {
            return true;
        }

        String jwtToken = RequestUtil.getCookieValue(req, Authorize.COOKIE_TOKEN);
        String subject = null;
        try {
            subject = jwtTokenProvider.parseSubject(jwtToken);
        } catch (BizException e) {
            adminLoginUserService.loginOut(resp, 0L);
            throw e;
        }
        if (StringUtils.isEmpty(subject)) {
            log.info("subject is null. request path => {}.", req.getServletPath());
            throw new BizException(ErrorCode.LOGIN_TOKEN_ERROR, "need.login");
        }
        Long userId = Long.valueOf(subject);
        if (userId == null) {
            adminLoginUserService.loginOut(resp, 0L);
            throw new BizException(ErrorCode.LOGIN_TOKEN_ERROR, "need.login");
        }
//        String userIdCookie = RequestUtil.getCookieValue(req, Authorize.ATTRIBUTE_USER_ID);
//        if (userIdCookie == null || !userIdCookie.equals(userId + "")) { //cookie数据不一致 重新登录
//            adminLoginUserService.loginOut(resp, userIdCookie == null ? 0L : Long.parseLong(userIdCookie));
//            throw new BizException(ErrorCode.LOGIN_TOKEN_ERROR, "need.login");
//        }

        AdminUserReply user = adminUserClient.getAdminUserById(userId);
        if (user == null || user.getId() == 0) {
            adminLoginUserService.loginOut(resp, 0L);
            throw new BizException(ErrorCode.LOGIN_TOKEN_ERROR, "need.login");
        }

        long orgId = baseCommonService.getOrgId();
        if (orgId != user.getOrgId()) {
            adminLoginUserService.loginOut(resp, user.getOrgId());
            throw new BizException(ErrorCode.LOGIN_TOKEN_ERROR, "need.login");
        }

        if (user.getStatus() == 2) {
            log.info("{} account be locked", userId);
            adminLoginUserService.loginOut(resp, userId);
            throw new BizException(ErrorCode.NO_PERMISSION, "account.be.locked");
        }

        req.setAttribute("adminUser", user);

        //verify ga or phone
        // 测试环境不开启ga验证
        if (gaEnable && verifyGaOrPhone) {
            if (!user.getBindGa() && !user.getBindPhone()) {
                log.info("unbind ga error. request path => {}.", req.getServletPath());
                throw new BizException(ErrorCode.UNBIND_GA_ERROR, ErrorCode.UNBIND_GA_ERROR.getDesc());
            }
        }
//        else { //不需要GA的 也无需权限验证
//            return true;
//        }

        //verify auth
        if (verifyAuth) {
            //root用户可以访问全部链接
            if (user.getAccountType() != AccountType.ROOT_ACCOUNT) {
                //判断登录用户是否有此路径的访问权限
                //获取当前用户的全部权限路径进行匹配
                ListAllAuthByUserIdReply allAuthReply = roleAuthService.listAllAuthByUserId(user.getOrgId(), user.getId());
                boolean hasPermission = false;
                if (authIds == null || authIds.length == 0) {
                    for (AuthInfo authInfo : allAuthReply.getAuthPathInfosList()) {
                        if (pathMatch(req.getServletPath(), authInfo.getPath())) {
                            hasPermission = true;
                            break;
                        }
                    }
                } else { //主要解决有一些通过路径无法处理的权限问题
                    List<Long> myAuthIds = allAuthReply.getAuthPathInfosList().stream()
                            .map(a -> a.getAuthId())
                            .collect(Collectors.toList());
                    for (long authId : authIds) {
                        if (myAuthIds.contains(authId)) {
                            hasPermission = true;
                            break;
                        }
                    }
                }


                if (!hasPermission) {
                    log.warn("org:{} user:{} no permission:{}", user.getOrgId(), user.getId(), req.getServletPath());
                    // 所有权限都不匹配，则提示权限异常，不可访问
                    throw new BizException(ErrorCode.NO_PERMISSION, ErrorCode.NO_PERMISSION.getDesc());
                }
            }
        }



        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

    @Override
    public void afterPropertiesSet() {

    }


    private String getRequestBody(InputStream stream) {
        String line = "";
        StringBuilder body = new StringBuilder();
        int counter = 0;

        // 读取POST提交的数据内容
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
        try {
            while ((line = reader.readLine()) != null) {
                body.append(line);
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return body.toString();
    }

    private void checkLoginIp(HttpServletRequest req, HttpServletResponse resp) {

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
            adminLoginUserService.loginOut(resp, 0L);
            log.warn("Admin login error: login ip not in whitelist. IP={}, OrgId={}", RequestUtil.getRealIP(req), orgId);
            throw new BizException(ErrorCode.IP_NOT_IN_WHITELIST_ERROR);
        }
    }


    /**
     * 匹配路径是否在控制域的范围内
     *
     * @param requestPath
     * @param authPathStr
     * @return
     */
    private static boolean pathMatch(String requestPath, String authPathStr) {
        List<String> authPaths = Lists.newArrayList(authPathStr.split(","));
        for (String authPath : authPaths) {
            if (PathPatternMatcher.isPattern(authPath) && PathPatternMatcher.match(authPath, requestPath)) {
                return true;
            }
            if (requestPath.equals(authPath)) {
                return true;
            }
        }

        return false;
    }
}
