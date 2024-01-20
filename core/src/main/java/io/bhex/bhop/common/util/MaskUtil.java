package io.bhex.bhop.common.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.util
 * @Author: ming.xu
 * @CreateDate: 2019/4/2 9:10 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
public class MaskUtil {

    public static final String MASK_EMAIL_REG = "(?<=.).(?=[^@]*?.@)"; // (?<=.{3}).(?=.*@) or (?<=.{2}).(?=[^@]*?.@)
    public static final String MASK_MOBILE_REG = "(\\d{3})\\d{4}(\\d{4})"; // \d(?=\d{4})
    public static final String MASK_SHORT_MOBILE_REG = "\\d*(\\d{4})";

    public static String mobileOutSensitive(String mobile) {
        if (StringUtils.isEmpty(mobile)) {
            return null;
        }
        if (mobile.length() == 11) {
            return mobile.replaceAll(MASK_MOBILE_REG, "$1****$2");
        } else {
            return mobile.replaceAll(MASK_SHORT_MOBILE_REG, "****$1");
        }
    }

    public static String emailOutSensitive(String email) {
        if (StringUtils.isEmpty(email)) {
            return null;
        }
        return email.replaceAll(MASK_EMAIL_REG, "*");
    }

    public static void main(String[] args) {
        System.out.println(emailOutSensitive("11231231232@啊.com"));
        System.out.println(mobileOutSensitive("18000000000"));
        System.out.println(mobileOutSensitive("18512345678"));
    }
}
