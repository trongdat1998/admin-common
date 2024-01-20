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
@Constraint(validatedBy = { StringInValid.Checker.class })
public @interface StringInValid {

    String message() default "bhop.validation.constraints.in.message";

    String[] value() default {};

    boolean allowEmpty() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Checker implements ConstraintValidator<StringInValid, String> {

        private Boolean allowEmpty;

        String[] inValues;

        @Override
        public void initialize(StringInValid parameters) {
            this.allowEmpty = parameters.allowEmpty();
            this.inValues = parameters.value();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (StringUtils.isEmpty(value)) {
                if (allowEmpty) {
                    return true;
                }
            }
            for (String v : inValues) {
                if (v.equals(value)) {
                    return true;
                }
            }
            return false;
        }

    }
}
