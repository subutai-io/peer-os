package org.safehaus.subutai.core.identity.impl.entity;


import java.io.Serializable;

import com.google.common.base.Preconditions;


/**
 * Created by talas on 3/7/15.
 */
public class UserPortalModulePK implements Serializable
{
    private String moduleKey;
    private String moduleName;


    public UserPortalModulePK()
    {
    }


    public UserPortalModulePK( final String moduleKey, final String moduleName )
    {
        Preconditions.checkNotNull( moduleKey, "Invalid parameter moduleKey" );
        Preconditions.checkNotNull( moduleName, "Invalid parameter moduleName" );
        this.moduleKey = moduleKey;
        this.moduleName = moduleName;
    }


    public String getModuleKey()
    {
        return moduleKey;
    }


    public String getModuleName()
    {
        return moduleName;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof UserPortalModulePK ) )
        {
            return false;
        }

        final UserPortalModulePK that = ( UserPortalModulePK ) o;

        return moduleKey.equals( that.moduleKey ) && moduleName.equals( that.moduleName );
    }


    @Override
    public int hashCode()
    {
        int result = moduleKey.hashCode();
        result = 31 * result + moduleName.hashCode();
        return result;
    }
}
