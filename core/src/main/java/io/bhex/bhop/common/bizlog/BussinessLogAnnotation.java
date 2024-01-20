package io.bhex.bhop.common.bizlog;


import java.lang.annotation.*;

/**
 * 标记需要做业务日志的方法
 *
 * @author bhex-admin
 * @date 2017-03-31 12:46
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface BussinessLogAnnotation {

    /**
     * 业务的名称,例如:"修改菜单"
     */
    String name() default "";

    /**
     * 被修改的实体的唯一标识
     */
    String entityId() default "";

    String remark() default "";

    String subType() default "";

    String opContent() default "";
}

