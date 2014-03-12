package com.neutrino.models.core_common;

@java.lang.annotation.Target(java.lang.annotation.ElementType.FIELD)
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface SelectableAttribute {
    Class<? extends CoreType> type() default CoreType.class;
}