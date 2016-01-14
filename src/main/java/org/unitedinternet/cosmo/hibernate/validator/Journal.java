package org.unitedinternet.cosmo.hibernate.validator;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target(METHOD) 
@Retention(RUNTIME)
@Constraint(validatedBy = JournalValidator.class)
@Documented
public @interface Journal {
    String message() default "has no VJOURNAL";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
