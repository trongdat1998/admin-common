package io.bhex.bhop.common.util.percent;

import io.bhex.bhop.common.config.LocaleMessageService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;

/**
 * @Description:对输入的百分数进行验证
 * @Date: 2018/11/8 下午9:28
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { Percentage.Checker.class })
public @interface Percentage {

    String message() default "bhop.validation.constraints.percentage.message";

    String min() default "0";
    String max() default "100";
    boolean allowNull() default false;

    Class<?>[]groups() default {};

    Class<? extends Payload>[]payload() default {};

    class Checker implements ConstraintValidator<Percentage, BigDecimal> {
        @Autowired
        private LocaleMessageService localeMessageService;

        private BigDecimal min;
        private BigDecimal max;
        private boolean allowNull;

        @Override
        public void initialize(Percentage parameters) {
            this.min = new BigDecimal(parameters.min());
            this.max = new BigDecimal(parameters.max());
            this.allowNull = parameters.allowNull();
        }

        @Override
        public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
            if (allowNull && value == null) {
                return true;
            }
            if (value == null) {
                return false;
            }
            boolean suc = value.compareTo(min.divide(new BigDecimal(100))) >= 0
                    && value.compareTo(max.divide(new BigDecimal(100))) <= 0;
            if (!suc) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(localeMessageService.getMessage("bhop.validation.constraints.percentage.message")).addConstraintViolation();
            }
            return suc;
        }
    }
}
