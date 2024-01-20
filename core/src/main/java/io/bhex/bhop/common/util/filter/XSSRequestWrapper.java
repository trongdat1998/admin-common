/**********************************
 *@项目名称: api-parent
 *@文件名称: io.bhex.broker.filter
 *@Date 2018/10/27
 *@Author peiwei.ren@bhex.io 
 *@Copyright（C）: 2018 BlueHelix Inc.   All rights reserved.
 *注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的。
 ***************************************/
package io.bhex.bhop.common.util.filter;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class XSSRequestWrapper extends HttpServletRequestWrapper {

    public XSSRequestWrapper(HttpServletRequest servletRequest) {
        super(servletRequest);
    }

    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);

        if (values == null) {
            return null;
        }

        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = XssShieldUtil.stripXss(values[i]);
        }

        return encodedValues;
    }

    @Override
    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        if (!Strings.isNullOrEmpty(value)) {
            value = value.trim();
        }
        return XssShieldUtil.stripXss(value);
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return XssShieldUtil.stripXss(value);
    }



    @Override
    public ServletInputStream getInputStream() throws IOException {
        String str = getRequestBody(super.getInputStream());
        if (str.startsWith("{") && str.endsWith("}")) {
            Map<String,Object> map = JSON.parseObject(str, Map.class);
            Map<String,Object> resultMap = new HashMap<>(map.size());
            for(String key : map.keySet()){
                Object val = map.get(key);
                if (map.get(key) instanceof String) {
                    String xssFilterStr = XssShieldUtil.stripXss(val.toString());
                    if (!xssFilterStr.equals(val.toString())) {
                        throw new XssFilterException(key);
                    }
                    resultMap.put(key, val.toString());
                } else {
                    resultMap.put(key,val);
                }
            }
            str = JSON.toJSONString(resultMap);
        }

        final ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes());
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return bais.read();
            }
            @Override
            public boolean isFinished() {
                return false;
            }
            @Override
            public boolean isReady() {
                return false;
            }
            @Override
            public void setReadListener(ReadListener listener) {
            }
        };
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
}
