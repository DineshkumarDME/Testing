package com.jules.project.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to mark methods that should be audited.
 * Audit information includes method name, caller, and timestamp,
 * and is sent to a Kafka topic.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    // We can add attributes here if needed in the future,
    // for example, to specify an event name or type.
    // String eventName() default "";
}
