package org.safehaus.subutai.core.identity.impl.dao;


import java.util.Arrays;
import java.util.List;

import org.safehaus.subutai.core.identity.api.PermissionGroup;
import org.safehaus.subutai.core.identity.impl.entity.PermissionEntity;
import org.safehaus.subutai.core.identity.impl.entity.PermissionPK;


/**
 * Created by talas on 2/5/15.
 */
public class PermissionTestUtils
{
    private static PermissionEntity testPermission =
            new PermissionEntity( "Can create test", PermissionGroup.ENVIRONMENT_PERMISSIONS,
                    "Specified user can create integration test" );
    private static PermissionEntity adminPermission =
            new PermissionEntity( "Admin", PermissionGroup.PEER_PERMISSIONS, "This user has admin role" );

    private static PermissionPK adminPermissionPk =
            new PermissionPK( adminPermission.getName(), adminPermission.getPermissionGroup() );

    private static PermissionPK testPermissionPk =
            new PermissionPK( testPermission.getName(), testPermission.getPermissionGroup() );


    public static PermissionEntity getTestPermission()
    {
        return testPermission;
    }


    public static PermissionEntity getAdminPermission()
    {
        return adminPermission;
    }


    public static PermissionPK getAdminPermissionPk()
    {
        return adminPermissionPk;
    }


    public static PermissionPK getTestPermissionPk()
    {
        return testPermissionPk;
    }


    public static List<PermissionEntity> getPermissions()
    {
        return Arrays.asList( testPermission, adminPermission );
    }
}
