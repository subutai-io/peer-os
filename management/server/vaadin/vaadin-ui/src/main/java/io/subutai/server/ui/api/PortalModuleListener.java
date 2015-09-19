package io.subutai.server.ui.api;


public interface PortalModuleListener
{

    public void moduleRegistered( final PortalModule module );

    public void moduleUnregistered( final PortalModule module );

    public void loadDependentModule( String moduleId );
}
