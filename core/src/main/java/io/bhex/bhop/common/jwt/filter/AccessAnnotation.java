package io.bhex.bhop.common.jwt.filter;

import java.lang.annotation.*;

/**
 * @Description: 验证顺序 内部访问 登录  GaOrPhone 权限
 * @Date: 2020/1/14 下午1:31
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AccessAnnotation {

    boolean internal() default false; //是否是内部访问 内部访问不做任何校验

    boolean verifyLogin() default true; //客户访问的最低权限

    boolean verifyGaOrPhone() default true; //

    boolean verifyAuth() default true; //是否校验权限，如果接口全站可访问 此值为false

    long[] authIds() default {}; //防止原有目录拦截问题

}
