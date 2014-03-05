package com.neutrino.models.core;

@java.lang.annotation.Target(java.lang.annotation.ElementType.FIELD)
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface SelectableAttribute {
    java.lang.String message() default "{javax.validation.constraints.NotNull.message}";

    java.lang.Class<?>[] groups() default {};

    java.lang.Class<? extends javax.validation.Payload>[] payload() default {};

    @java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.ANNOTATION_TYPE, java.lang.annotation.ElementType.CONSTRUCTOR, java.lang.annotation.ElementType.PARAMETER})
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Documented
    static @interface List {
        javax.validation.constraints.NotNull[] value();
    }
}