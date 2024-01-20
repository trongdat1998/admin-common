package io.bhex.bhop.common.util.filter;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class XssShieldUtil {

    private static Pattern[] patterns = new Pattern[]{
            Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(window\\.location|window\\.|\\.location|window\\.open\\()*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    };

    private static Pattern[] strictPatterns = new Pattern[]{
            Pattern.compile("<(no)?script[^>]*>.*?</(no)?script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(javascript:|vbscript:|view-source:)*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<(\"[^\"]*\"|\'[^\']*\'|[^\'\">])*>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("(window\\.location|window\\.|\\.location|document\\.cookie|document\\.|alert\\(.*?\\)|window\\.open\\()*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("<+\\s*\\w*\\s*(oncontrolselect|oncopy|oncut|ondataavailable|ondatasetchanged|ondatasetcomplete|ondblclick|ondeactivate|ondrag|ondragend|ondragenter|ondragleave|ondragover|ondragstart|ondrop|οnerrοr=|onerroupdate|onfilterchange|onfinish|onfocus|onfocusin|onfocusout|onhelp|onkeydown|onkeypress|onkeyup|onlayoutcomplete|onload|onlosecapture|onmousedown|onmouseenter|onmouseleave|onmousemove|onmousout|onmouseover|onmouseup|onmousewheel|onmove|onmoveend|onmovestart|onabort|onactivate|onafterprint|onafterupdate|onbefore|onbeforeactivate|onbeforecopy|onbeforecut|onbeforedeactivate|onbeforeeditocus|onbeforepaste|onbeforeprint|onbeforeunload|onbeforeupdate|onblur|onbounce|oncellchange|onchange|onclick|oncontextmenu|onpaste|onpropertychange|onreadystatechange|onreset|onresize|onresizend|onresizestart|onrowenter|onrowexit|onrowsdelete|onrowsinserted|onscroll|onselect|onselectionchange|onselectstart|onstart|onstop|onsubmit|onunload)+\\s*=+", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
    };




    public static String stripXss(String value) {
        if (value != null) {
            // NOTE: It's highly recommended to use the ESAPI library and uncomment the following line to
            // avoid encoded attacks.
            // value = ESAPI.encoder().canonicalize(value);

            // Avoid null characters
            value = value.replaceAll("\0", "");
            // Remove all sections that match a pattern
            for (Pattern scriptPattern : patterns) {
                value = scriptPattern.matcher(value).replaceAll("");
            }
        }
        return value;
    }


    public static String stripXssStrict(String value) {
        if (value != null) {
            // NOTE: It's highly recommended to use the ESAPI library and uncomment the following line to
            // avoid encoded attacks.
            // value = ESAPI.encoder().canonicalize(value);

            // Avoid null characters
            value = value.replaceAll("\0", "");
            // Remove all sections that match a pattern
            for (Pattern scriptPattern : strictPatterns) {
                value = scriptPattern.matcher(value).replaceAll("");
            }
        }
        return value;
    }

    public static void main(String[] args) {

        String value = null;
//        value = XssShieldUtil.stripXssStrict("<script language=text/javascript>alert(document.cookie);</script>");
//        System.out.println("type-1: '" + value + "'");
//
//        value = XssShieldUtil.stripXss("<script src='' οnerrοr='alert(document.cookie)'></script>");
//        System.out.println("type-2: '" + value + "'");
//
//        value = XssShieldUtil.stripXss("<script>alert(123)");
//        System.out.println("type-22: '" + value + "'");
//
//        value = XssShieldUtil.stripXss("</script>");
//        System.out.println("type-3: '" + value + "'");
//
//        value = XssShieldUtil.stripXss(" eval(abc);");
//        System.out.println("type-4: '" + value + "'");
//
//        value = XssShieldUtil.stripXss(" expression(abc);");
//        System.out.println("type-5: '" + value + "'");
//
//        value = XssShieldUtil.stripXssStrict("<img src='https://www.baidud.com/j.png'></img>");
//        System.out.println("type-6: '" + value + "'");
//
//        value = XssShieldUtil.stripXss("<img src='' οnerrοr='alert(document.cookie);'/>");
//        System.out.println("type-7: '" + value + "'");
//
//        value = XssShieldUtil.stripXss("<img src='' οnerrοr='alert(document.cookie);'>");
//        System.out.println("type-8: '" + value + "'");
//
//        value = XssShieldUtil.stripXss("<script language=text/javascript>alert(document.cookie);");
//        System.out.println("type-9: '" + value + "'");
//
//        value = XssShieldUtil.stripXss("<script>window.location='url'");
//        System.out.println("type-10: '" + value + "'");
//
//        value = XssShieldUtil.stripXssStrict(" οnlοad='alert(\"abc\");");
//        System.out.println("type-11: '" + value + "'");
//
//        value = XssShieldUtil.stripXss("<img src=x<!--'<\"-->>");
//        System.out.println("type-12: '" + value + "'");
//        System.out.println("type-12: '" + value.length() + "'" + "<img src=x<!--'<\"-->>".length());
//
//        value = XssShieldUtil.stripXss("<=img onstop=");
//        System.out.println("type-13: '" + value + "'");

        value = XssShieldUtil.stripXss("window.iop");
        System.out.println("type-13: '" + value + "'");
    }
}
