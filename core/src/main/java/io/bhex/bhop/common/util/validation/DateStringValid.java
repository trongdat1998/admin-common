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
@Constraint(validatedBy = { DateStringValid.Checker.class })
public @interface DateStringValid {

    String message() default "bhop.validation.constraints.date.message";

    boolean allowEmpty() default false;

    String pattern() default "yyyy-MM-dd";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Checker implements ConstraintValidator<DateStringValid, String> {

        private Boolean allowEmpty;

        private String pattern;

        @Override
        public void initialize(DateStringValid parameters) {
            this.allowEmpty = parameters.allowEmpty();
            this.pattern = parameters.pattern();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null || value.trim().equals("")) {
                if (allowEmpty) {
                    return true;
                }
            }
            return ValidUtil.isDateStr(value, pattern);
        }


    }

}
