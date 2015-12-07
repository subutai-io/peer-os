package io.subutai.common.datatypes;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Created by talas on 12/7/15.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.PARAMETER )
public @interface Relation
{
    public enum RelationWeight
    {
        FAKE, INDIFFERENT, TRUTH
    }

    String context() default "";
}
