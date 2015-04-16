package org.safehaus.subutai.server.ui.api;


import java.util.List;


public interface PortalModuleService
{

    public PortalModule getModule( final String pModuleId );

    public List<PortalModule> getModules();

    public List<PortalModule> getCoreModules();

    public void addListener( final PortalModuleListener listener );

    public void removeListener( final PortalModuleListener listener );

    public void loadDependentModule( String moduleId );
}
