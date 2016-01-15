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
public @interface RelationDeclaration
{
    /**
     * Relationship query, that queries permissions exists between entities
     */
    String rql() default "";

    RelationCondition[] conditions();
}
