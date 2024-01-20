package io.bhex.bhop.common.util.validation;

import io.bhex.bhop.common.util.filter.XssShieldUtil;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ValidUtil {

    public static boolean isSimpleInput(String str) {
        if (str == null) {
            return false;
        }
        if (!str.equals(XssShieldUtil.stripXssStrict(str))) {
            return false;
        }
        return true;
    }

    public static boolean isPhone(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        String regex = "^[0-9 +]+$";
        return str.matches(regex);
    }

    public static boolean isUrl(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        if (!str.equals(XssShieldUtil.stripXss(str))) {
            return false;
        }
        return true;
    }

    public static boolean isToken(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        String regex = "^[0-9A-Z-_]+$";
        return str.matches(regex);
    }

    public static boolean isTokenName(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        String regex = "^[0-9a-zA-Z-_]+$";
        return str.trim().matches(regex);
    }

    public static boolean isSymbol(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        String regex = "^[0-9A-Z-_/]+$";
        return str.matches(regex);
    }

    public static boolean isSymbolName(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        String regex = "^[0-9A-Za-z-_]+$";
        return str.matches(regex);
    }

    public static boolean isDateStr(String dateStr, String formatStr) {
        if (org.springframework.util.StringUtils.isEmpty(dateStr)) {
            return false;
        }
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        Date newdate = null;
        try {
            newdate = format.parse(dateStr);
            return dateStr.equals(format.format(newdate));
        } catch (ParseException e) {

        }
        return false;
    }

    public static void main(String[] args) {
        //System.out.println(isPhone("+19 12060005"));
        System.out.println(isTokenName("XRP-SWAP_dfUSDT"));
//        System.out.println(isSymbol("HT1227PS270"));
//        System.out.println(isSymbol("BCH-SWAP-USDT"));
//        System.out.println(isSimpleInput("1=1'"));
    }
}
