package io.bhex.bhop.common.util.validation;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;

@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { BigDecimalStringValid.Checker.class })
public @interface BigDecimalStringValid {

    String message() default "bhop.validation.constraints.number.message";

    boolean allowEmpty() default false;

    Class<?>[]groups() default {};

    Class<? extends Payload>[]payload() default {};

    class Checker implements ConstraintValidator<BigDecimalStringValid, String> {

        private Boolean allowEmpty;

        @Override
        public void initialize(BigDecimalStringValid parameters) {
            this.allowEmpty = parameters.allowEmpty();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.trim().equals("")) {
                if (allowEmpty) {
                    return true;
                }
            }
            try {
                new BigDecimal(value.trim());
            } catch (Exception e) {
                return false;
            }
            return true;
        }
    }

}
