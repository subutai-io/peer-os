package org.safehaus.subutai.common.protocol;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation for fields that should be skipped when deserializing json to POJO using GSON
 */
@Target( value = ElementType.FIELD )
@Retention( value = RetentionPolicy.RUNTIME )
public @interface SkipNull {}
