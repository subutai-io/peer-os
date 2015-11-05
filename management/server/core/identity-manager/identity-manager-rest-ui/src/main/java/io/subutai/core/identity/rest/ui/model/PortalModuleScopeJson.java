package io.subutai.core.identity.rest.ui.model;

import com.google.common.base.Preconditions;
import io.subutai.core.identity.api.PortalModuleScope;

public class PortalModuleScopeJson implements PortalModuleScope
{

    private String moduleKey;

    private String moduleName;


    public PortalModuleScopeJson( final String moduleKey, final String moduleName )
    {
        Preconditions.checkNotNull(moduleKey, "Invalid argument moduleKey");
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
    public String toString()
    {
        return "UserPortalModuleEntity{" +
                "moduleKey='" + moduleKey + '\'' +
                ", moduleName='" + moduleName + '\'' +
                '}';
    }
}