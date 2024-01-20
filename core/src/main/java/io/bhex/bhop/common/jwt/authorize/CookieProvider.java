/*
 ************************************
 * @项目名称: bhcard
 * @文件名称: CookieUtil
 * @Date 2018/05/27
 * @Author will.zhao@bhex.io
 * @Copyright（C）: 2018 BlueHelix Inc.   All rights reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的。
 **************************************
 */

package io.bhex.bhop.common.jwt.authorize;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class CookieProvider {

    private String domain;

    private boolean secure;

    public CookieProvider(String domain, boolean secure) {
        this.domain = domain;
        this.secure = secure;
    }

    public void create(HttpServletResponse httpServletResponse, String name, String value, boolean httpOnly) {
        Cookie cookie = new Cookie(name, value);
        cookie.setSecure(secure);//secure=true => work on HTTPS only.
        cookie.setHttpOnly(httpOnly);//invisible to JavaScript.
        cookie.setMaxAge(-1);//maxAge=0: expire cookie now, maxAge<0: expire cookie on browser exit.
        //cookie.setDomain(domain);//visible to domain only
        cookie.setPath("/");//visible to all paths
        httpServletResponse.addCookie(cookie);
    }

    public void clear(HttpServletResponse response, String name) {


        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setSecure(secure);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        //cookie.setDomain(domain);
        response.addCookie(cookie);
    }

}


