package io.bhex.bhop.common.bizlog;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import io.bhex.base.admin.common.BusinessLog;
import io.bhex.base.admin.common.SaveLogRequest;
import io.bhex.bhop.common.config.OrgInstanceConfig;
import io.bhex.bhop.common.enums.AdminPlatformEnum;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.grpc.client.BusinessLogClient;
import io.bhex.bhop.common.jwt.authorize.Authorize;
import io.bhex.bhop.common.service.BaseCommonService;
import io.bhex.bhop.common.util.RequestUtil;
import io.bhex.bhop.common.util.ResultModel;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.env.Environment;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Aspect
@Component
public class LogAop {

    @Resource
    private Environment environment;
    @Resource
    private OrgInstanceConfig orgInstanceConfig;
    @Resource
    private BusinessLogClient businessLogClient;
    @Resource
    private BaseCommonService baseCommonService;


    //@Pointcut(value = "execution(* io.bhex.*..*Controller.*(..))")
    //@Pointcut(value = "@annotation(io.bhex.bhop.common.bizlog.BussinessLogAnnotation)")
    @Pointcut(value = "@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void cutService() {
    }

    @Pointcut(value = "@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void cutService2() {
    }

    @Pointcut(value = "@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public void cutService3() {
    }

    @AfterReturning(pointcut = "cutService() || cutService2() || cutService3()", returning = "retVal")
    public void afterReturningAdvice(JoinPoint point, Object retVal) throws Exception {
        try {
            handle(point, retVal);
        } catch (Exception e) {
            try {
                log.error("handle admin user action aspect error, req:{}", getRequestInfo(point), e);
            } catch (Exception ex) {

            }
        }
    }

    @AfterThrowing(pointcut = "cutService() || cutService2() || cutService3()", throwing = "ex")
    public void afterThrowingAdvice(JoinPoint point, Throwable ex) {
        try {
            handle(point, ex);
        } catch (Exception e) {
            try {
                log.error("handle admin user action aspect error, req:{}", getRequestInfo(point), e);
            } catch (Exception exc) {

            }
        }
    }

    private JSONObject getRequestInfo(JoinPoint point) throws Exception {
        Method currentMethod = getCurrentMethod(point);
        Object[] args = point.getArgs();
        Annotation[][] annotations = currentMethod.getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation[] arr = annotations[i];
            if (arr.length == 0) {
                continue;
            }
            for (Annotation annotation : arr) {
                if (!(annotation instanceof RequestBody)) {
                    continue;
                }
                String className = point.getTarget().getClass().getSimpleName();
                String methodName = currentMethod.getName();
                //String content = className + "/" + methodName + ":"+(args[i] != null ? JSON.toJSONString(args[i]) : "");
                JSONObject jo = JSON.parseObject(args[i] != null ? JSON.toJSONString(args[i]) : "{}");
                jo.put("class", className);
                jo.put("method", methodName);
                return jo;
            }
        }
        JSONObject jo = new JSONObject();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Enumeration<String> pNames = request.getParameterNames();
        while (pNames != null && pNames.hasMoreElements()) {
            String name = pNames.nextElement();
            jo.put(name, request.getParameter(name));
        }
        jo.put("class", point.getTarget().getClass().getSimpleName());
        jo.put("method", currentMethod.getName());
        return jo;
    }

    private Method getCurrentMethod(JoinPoint point) throws Exception {
        //获取拦截的方法名
        Signature sig = point.getSignature();
        MethodSignature msig = (MethodSignature) sig;
        Object target = point.getTarget();
        Method currentMethod = target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
        return currentMethod;
    }

    private String getBussinessName(BussinessLogAnnotation logAnnotation, JoinPoint point) throws Exception {
        Method currentMethod = getCurrentMethod(point);
        String bizName = logAnnotation == null ? "" : logAnnotation.name();
        if (StringUtils.isNotEmpty(bizName)) {
            bizName = parseOpContent(bizName, currentMethod, point.getArgs());
            return bizName;
        }
        return currentMethod.getName();
    }

    private void handle(JoinPoint point, Object result) throws Exception {

        Method currentMethod = getCurrentMethod(point);
        RequestMapping rmapping = currentMethod.getAnnotation(RequestMapping.class);
        PostMapping pmapping = currentMethod.getAnnotation(PostMapping.class);
        if (rmapping == null && pmapping == null) {
            return;
        }

        BussinessLogAnnotation annotation = currentMethod.getAnnotation(BussinessLogAnnotation.class);
        if (annotation == null && point.getTarget().getClass().getAnnotation(ExcludeLogAnnotation.class) != null) {
            return;
        }

        if (annotation == null && currentMethod.getAnnotation(ExcludeLogAnnotation.class) != null) {
            return;
        }

        //获取操作名称
        String bussinessName = getBussinessName(annotation, point);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        if (bussinessName.contains("healthCheck") || bussinessName.contains("metrics")
                || Strings.nullToEmpty(request.getHeader("user-agent")).contains("HealthChecker")
                || Strings.nullToEmpty(request.getHeader("user-agent")).contains("Prometheus")) { //内部健康检查 不处理
            return;
        }

        String subType = annotation == null ? "" : annotation.subType();
        String opContent = annotation == null ? "" : annotation.opContent();

        JSONObject requestJSON = getRequestInfo(point);
        // log.info("request:{}", requestJSON.toJSONString());
        BusinessLog.Builder logBuilder = BusinessLog.newBuilder();
        logBuilder.setRequestInfo(requestJSON.toJSONString());


        String username = RequestUtil.getCookieValue(request, Authorize.LOGIN_COOKIE);

        if (result instanceof ResultModel) {
            ResultModel resultModel = (ResultModel) result;
            logBuilder.setResultCode(resultModel.getCode());
            logBuilder.setResultMsg(resultModel.getMsg());

            if (bussinessName.equals("op.login")) { //登录从请求信息中取用户名
                username = requestJSON.getString("username");
            }
        } else if (result instanceof BizException) {
            BizException e = (BizException) result;
            logBuilder.setResultCode(e.getCode());
            logBuilder.setResultMsg("");
        } else if (result instanceof Exception) {
            logBuilder.setResultCode(1);
        }

        String entityId = annotation != null ? parseOpContent(annotation.entityId(), currentMethod, point.getArgs()) : "";
        if (StringUtils.isEmpty(entityId) && requestJSON != null && requestJSON.containsKey("userId")) {
            entityId = Strings.nullToEmpty(requestJSON.getString("userId"));
        }

        Long orgId = getOrgId(); //url错误导致的
        logBuilder.setOrgId(orgId != null ? orgId : -99);
        logBuilder.setUsername(username == null ? "" : username);
        logBuilder.setOpType(bussinessName);
        logBuilder.setEntityId(entityId);
        logBuilder.setRemark(parseOpContent(opContent, currentMethod, point.getArgs()));
        logBuilder.setIp(baseCommonService.getRemoteIp(request));
        logBuilder.setVisible(StringUtils.isNotEmpty(opContent));
        String referer = request.getHeader("Referer") == null ? "" : request.getHeader("Referer");
        logBuilder.setRequestUrl(referer);
        logBuilder.setSubType(parseOpContent(subType, currentMethod, point.getArgs()));
        logBuilder.setUserAgent(Strings.nullToEmpty(request.getHeader("user-agent")));

        Object startTimeObj = request.getAttribute("START_TIME");
        long consumeTimeMs = startTimeObj != null ? (System.currentTimeMillis() - Long.parseLong(startTimeObj.toString())) : 0;
        log.info("org:{}\tuser:{}\top:{}\tremark:{}\treq:{}\treferer:{}\tua:{}\tip:{}\tresult:{}\tconsume:{}ms", orgId, username, bussinessName,
                logBuilder.getRemark(), requestJSON, referer, logBuilder.getUserAgent(), logBuilder.getIp(),
                logBuilder.getResultCode() + " " + logBuilder.getResultMsg(),
                consumeTimeMs > 0 ? consumeTimeMs : "--");

        String methodName = currentMethod.getName();
        boolean isQueryMethod = methodName.startsWith("get") || methodName.startsWith("query") || methodName.startsWith("find")
                || methodName.startsWith("list") || methodName.startsWith("show") || methodName.startsWith("select");
        if (!isQueryMethod) {
            businessLogClient.saveLog(SaveLogRequest.newBuilder().setBusinessLog(logBuilder.build()).build());
        }
    }


    private String parseOpContent(String opContent, Method method, Object[] args) {
        if (StringUtils.isEmpty(opContent)) {
            return "";
        }
        List<String> keys = new ArrayList<>();
        Matcher matcher = PARAM_PLACEHOLDER_PATTERN.matcher(opContent);
        while (matcher.find()) {
            String key = matcher.group();
            keys.add(key.replace("{", "").replace("}", ""));
        }
        if (CollectionUtils.isEmpty(keys)) {
            return opContent;
        }

        List<String> params = new ArrayList<>();
        for (String key : keys) {
            params.add(parseKey(key, method, args));
        }


        return render(opContent, params);
    }

    private String parseKey(String key, Method method, Object[] args) {
        if (key == null || !key.startsWith("#")) {
            return key;
        }


        //获取被拦截方法参数名列表(使用Spring支持类库)
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paraNameArr = u.getParameterNames(method);

        //使用SPEL进行key的解析
        ExpressionParser parser = new SpelExpressionParser();
        //SPEL上下文
        StandardEvaluationContext context = new StandardEvaluationContext();
        //把方法参数放入SPEL上下文中
        for (int i = 0; i < paraNameArr.length; i++) {
            context.setVariable(paraNameArr[i], args[i]);
        }
        return parser.parseExpression(key).getValue(context, String.class);
    }

    //private static final Pattern PARAM_PLACEHOLDER_PATTERN = Pattern.compile("\\{([A-Za-z_$]+\\.[A-Za-z_$\\d]*)\\}");

    private static final Pattern PARAM_PLACEHOLDER_PATTERN = Pattern.compile("\\{.*?\\}");

    public static String render(String content, List<String> params) {
        if (Collections.isEmpty(params)) {
            return content;
        }
        Matcher matcher = PARAM_PLACEHOLDER_PATTERN.matcher(content);
        int index = -1;
        while (matcher.find()) {
            String key = matcher.group();
            content = content.replace(key, params.get(++index));
        }
        return content;
    }

    public AdminPlatformEnum getAdminPlatform() {
        String serverName = environment.getProperty("spring.application.name", String.class);
        return AdminPlatformEnum.getByServerName(serverName);
    }

    public Long getOrgId() {
        AdminPlatformEnum platform = getAdminPlatform();
        if (platform.equals(AdminPlatformEnum.SAAS_ADMIN_PLATFROM)) { //saas平台没有org的概念
            return 0L;
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String httpServerName = request.getServerName();
        if (platform.equals(AdminPlatformEnum.BROKER_ADMIN_PLATFROM)) {
            Long orgId = orgInstanceConfig.getBrokerIdByDomain(httpServerName);
//            if (orgId == null || orgId == 0) {
//                orgId = (Long) request.getAttribute("orgId");
//            }
            return orgId;
        }
        if (platform.equals(AdminPlatformEnum.EXCHANGE_ADMIN_PLATFROM)) {
            return orgInstanceConfig.getExchangeIdByDomain(httpServerName);
        }

        return null;
    }


}

