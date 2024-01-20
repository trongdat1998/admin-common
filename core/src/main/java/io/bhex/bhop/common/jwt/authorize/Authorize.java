/*
 ************************************
 * @项目名称: bhcard
 * @文件名称: Authorize
 * @Date 2018/06/25
 * @Author will.zhao@bhex.io
 * @Copyright（C）: 2018 BlueHelix Inc.   All rights reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的。
 **************************************
 */
package io.bhex.bhop.common.jwt.authorize;

public class Authorize {

    public static final String JWT_SECRET = "authorize.token.jwt.secret";

    public static final String JWT_TOKEN_EXPIRE = "authorize.token.jwt.expire_in_seconds";

    public static final String IGNORE_RESOURCE = "authorize.ignore.resource";

    public static final String GA_IGNORE_RESOURCE = "authorize.ignore.garesource";

    public static final String GA_ENABLE = "authorize.gaenable";

    public static final String AUTHORIZE_KEY = "au_jwt_key";

    public static final String COOKIE_TOKEN = "auth_token";

    public static final String LOGIN_COOKIE = "login_key";

    public static final String ATTRIBUTE_USER_ID = "user_id";
}
