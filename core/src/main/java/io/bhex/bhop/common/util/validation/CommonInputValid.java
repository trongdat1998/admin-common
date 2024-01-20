package io.bhex.bhop.common.util.validation;

import io.bhex.bhop.common.config.LocaleMessageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 普通输入的校验，如果input为空则直接过
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { CommonInputValid.Checker.class })
public @interface CommonInputValid {

    String message() default "{bhop.validation.constraints.commoninput.message}";

    int maxLength() default 128;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Checker implements ConstraintValidator<CommonInputValid, String> {

        private int maxLength;

        @Autowired
        private LocaleMessageService localeMessageService;

        @Override
        public void initialize(CommonInputValid parameters) {
            this.maxLength = parameters.maxLength();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (StringUtils.isEmpty(value)) {
                return true;
            }
            boolean suc = true;
            if (value.length() > maxLength) {
                suc = false;
            }
            if (suc) {
                suc = ValidUtil.isSimpleInput(value);
            }
            if (!suc) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(localeMessageService.getMessage("bhop.validation.constraints.commoninput.message")).addConstraintViolation();
            }
            return suc;
        }

    }
}
