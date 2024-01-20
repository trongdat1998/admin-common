/*
 ************************************
 * @项目名称: bhcard
 * @文件名称: RequestUtil
 * @Date 2018/05/22
 * @Author will.zhao@bhex.io
 * @Copyright（C）: 2018 BlueHelix Inc.   All rights reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的。
 **************************************
 */
package io.bhex.bhop.common.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class RequestUtil {

    public static String getRealIP(HttpServletRequest req) {
        String ip = getFirstNonBlankHeader(req, "X-Real-IP", "x-real-ip",
                "X-Forwarded-For", "x-forwarded-for");
        if (ip == null) {
            ip = req.getRemoteAddr();
            if (ip == null) {
                ip = "";
            }
        }
        int idx = ip.indexOf(",");
        ip = (idx != -1) ? ip.substring(0, idx).trim() : ip;
        return format(ip);
    }

    private static String getFirstNonBlankHeader(HttpServletRequest req, String... headerNames) {
        if (req == null) {
            return null;
        }
        for (String name : headerNames) {
            String value = req.getHeader(name);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    public static String format(String value) {
        return StringUtils.isNotEmpty(value) ? value.replaceAll("[\r\n]", "") : value;
    }

    public static String getCookieValue(HttpServletRequest request, String name) {
        Cookie cookie = WebUtils.getCookie(request, name);
        return cookie != null ? cookie.getValue() : null;
    }
}


