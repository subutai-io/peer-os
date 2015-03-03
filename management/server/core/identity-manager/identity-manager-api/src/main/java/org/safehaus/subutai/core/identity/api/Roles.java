package org.safehaus.subutai.core.identity.api;


public enum Roles
{
    ADMIN( "admin" ), MANAGER( "manager" ), VIEWER( "viewer" );
    private String roleName;


    Roles( final String roleName )
    {
        this.roleName = roleName;
    }


    public String getRoleName()
    {
        return roleName;
    }
}
