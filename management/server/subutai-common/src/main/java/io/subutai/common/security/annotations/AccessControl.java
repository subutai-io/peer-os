package io.subutai.common.security.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 *
 */
@Retention( RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD})

public @interface AccessControl
{
    String objects();
    String operations();
}
