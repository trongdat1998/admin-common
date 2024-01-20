package io.bhex.bhop.common.bizlog;

import java.lang.annotation.*;

/**
 * @Description:此注解代表此类或方法不用记录访问日志
 * @Date: 2019/12/27 下午2:35
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ExcludeLogAnnotation {

}
