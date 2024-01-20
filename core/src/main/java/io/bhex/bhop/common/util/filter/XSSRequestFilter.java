/**********************************
 *@项目名称: api-parent
 *@文件名称: io.bhex.broker.filter
 *@Date 2018/10/27
 *@Author peiwei.ren@bhex.io 
 *@Copyright（C）: 2018 BlueHelix Inc.   All rights reserved.
 *注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的。
 ***************************************/
package io.bhex.bhop.common.util.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class XSSRequestFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(new XSSRequestWrapper((HttpServletRequest) request), response);
    }

}
