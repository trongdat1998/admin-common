package io.bhex.bhop.common.util;

import java.util.SortedSet;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.util
 * @Author: ming.xu
 * @CreateDate: 09/10/2018 2:35 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
public class UrlUtils {

    /**
     * 匹配路径是否在控制域的范围内
     *
     * @param urls
     * @param path
     * @return
     */
    public static boolean urlMatch(SortedSet<String> urls, String path) {

        if (null == urls || urls.isEmpty()) return false;

        SortedSet<String> hurl = urls.headSet(path + "\0");
        SortedSet<String> turl = urls.tailSet(path + "\0");

        if (hurl.size() > 0) {
            String before = hurl.last();
            if (pathMatch(path, before)) return true;
        }

        if (turl.size() > 0) {
            String after = turl.first();
            if (pathMatch(path, after)) return true;
        }

        for (String url: urls) {
            if (pathMatch(path, url)) {
                return true;
            }
        }
        return false;
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
