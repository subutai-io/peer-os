package org.safehaus.subutai.core.identity.impl.entity;


import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.safehaus.subutai.core.identity.api.PortalModuleScope;

import com.google.common.base.Preconditions;


@Entity
@Access( AccessType.FIELD )
@Table( name = "portal_module" )
public class PortalModuleScopeEntity implements PortalModuleScope, Serializable
{
    @Id
    @Column( name = "module_key" )
    private String moduleKey;

    @Column( name = "module_name" )
    private String moduleName;


    public PortalModuleScopeEntity( final String moduleKey, final String moduleName )
    {
        Preconditions.checkNotNull( moduleKey, "Invalid argument moduleKey" );
        Preconditions.checkNotNull( moduleName, "Invalid argument moduleName" );
        this.moduleKey = moduleKey;
        this.moduleName = moduleName;
    }


    @Override
    public String getModuleKey()
    {
        return moduleKey;
    }


    @Override
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
        if ( !( o instanceof PortalModuleScopeEntity ) )
        {
            return false;
        }

        final PortalModuleScopeEntity that = ( PortalModuleScopeEntity ) o;

        return moduleKey.equals( that.moduleKey ) && moduleName.equals( that.moduleName );
    }


    @Override
    public int hashCode()
    {
        int result = moduleKey.hashCode();
        result = 31 * result + moduleName.hashCode();
        return result;
    }


    @Override
    public String toString()
    {
        return "UserPortalModuleEntity{" +
                "moduleKey='" + moduleKey + '\'' +
                ", moduleName='" + moduleName + '\'' +
                '}';
    }
}
