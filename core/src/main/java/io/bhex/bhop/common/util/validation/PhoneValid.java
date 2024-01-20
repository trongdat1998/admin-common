package io.bhex.bhop.common.util.validation;

import org.apache.commons.lang3.StringUtils;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { PhoneValid.Checker.class })
public @interface PhoneValid {

    String message() default "bhop.validation.constraints.phone.message";
    boolean allowEmpty() default false;
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Checker implements ConstraintValidator<PhoneValid, String> {

        private Boolean allowEmpty;

        @Override
        public void initialize(PhoneValid parameters) {
            this.allowEmpty = parameters.allowEmpty();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (StringUtils.isEmpty(value)) {
                if (allowEmpty) {
                    return true;
                }
            }
            return ValidUtil.isPhone(value);
        }

    }
}
