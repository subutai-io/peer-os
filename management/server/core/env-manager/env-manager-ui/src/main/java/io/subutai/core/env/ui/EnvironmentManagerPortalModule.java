package io.subutai.core.env.ui;


import java.io.File;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.util.FileUtil;
import io.subutai.core.env.api.EnvironmentEventListener;
import io.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Component;


public class EnvironmentManagerPortalModule implements PortalModule, EnvironmentEventListener
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentManagerPortalModule.class.getName() );
    private final static String MODULE_IMAGE = "environment.jpg";
    private final static String MODULE_NAME = "Environment";
    private final EnvironmentManager environmentManager;
    private final PeerManager peerManager;
    private final TemplateRegistry templateRegistry;


    public EnvironmentManagerPortalModule( final EnvironmentManager environmentManager, final PeerManager peerManager,
                                           final TemplateRegistry templateRegistry )
    {
        this.environmentManager = environmentManager;
        this.peerManager = peerManager;
        this.templateRegistry = templateRegistry;
    }


    @Override
    public String getId()
    {
        return MODULE_NAME;
    }


    @Override
    public String getName()
    {
        return MODULE_NAME;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        return new EnvironmentManagerComponent( environmentManager, peerManager, templateRegistry );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }


    @Override
    public void onEnvironmentCreated( final Environment environment )
    {
        LOG.info( String.format( "Environment created: %s", environment ) );
    }


    @Override
    public void onEnvironmentGrown( final Environment environment, final Set<ContainerHost> newContainers )
    {
        LOG.info( String.format( "Environment grown: %s, containers: %s", environment, newContainers ) );
    }


    @Override
    public void onContainerDestroyed( final Environment environment, final UUID containerId )
    {
        LOG.info( String.format( "Container destroyed: %s, environment: %s", containerId, environment ) );
    }


    @Override
    public void onEnvironmentDestroyed( final UUID environmentId )
    {
        LOG.info( String.format( "Environment destroyed: %s", environmentId ) );
    }
}
