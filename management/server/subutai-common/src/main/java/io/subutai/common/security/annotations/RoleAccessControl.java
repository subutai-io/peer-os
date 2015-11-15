package io.subutai.common.security.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.objects.PermissionOperation;


/**
 *
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.METHOD } )

public @interface RoleAccessControl
{
    PermissionObject[] objects();

    PermissionOperation[] operations();
}
