package io.bhex.bhop.common.util.validation;

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
@Constraint(validatedBy = { IntInValid.Checker.class })
public @interface IntInValid {

    String message() default "bhop.validation.constraints.in.message";

    int[] value() default {};

    boolean allowZero() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Checker implements ConstraintValidator<IntInValid, Integer> {

        private Boolean allowZero;

        int[] inValues;

        @Override
        public void initialize(IntInValid parameters) {
            this.allowZero = parameters.allowZero();
            this.inValues = parameters.value();
        }

        @Override
        public boolean isValid(Integer value, ConstraintValidatorContext context) {
            if (value == 0) {
                if (allowZero) {
                    return true;
                }
            }
            for (int v : inValues) {
                if (v == value) {
                    return true;
                }
            }
            return false;
        }

    }
}
