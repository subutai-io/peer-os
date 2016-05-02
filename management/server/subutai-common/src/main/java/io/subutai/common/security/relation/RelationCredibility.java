package io.subutai.common.security.relation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention( RetentionPolicy.RUNTIME )
@Target({ElementType.METHOD, ElementType.TYPE_USE} )
public @interface RelationCredibility
{
}
