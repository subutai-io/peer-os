package io.subutai.core.environment.ui;


import java.io.File;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Component;

import io.subutai.common.environment.Environment;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.util.FileUtil;
import io.subutai.core.environment.api.EnvironmentEventListener;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registry.api.TemplateRegistry;
import io.subutai.server.ui.api.PortalModule;


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
    public void onEnvironmentGrown( final Environment environment, final Set<EnvironmentContainerHost> newContainers )
    {
        LOG.info( String.format( "Environment grown: %s, containers: %s", environment, newContainers ) );
    }


    @Override
    public void onContainerDestroyed( final Environment environment, final String containerId )
    {
        LOG.info( String.format( "Container destroyed: %s, environment: %s", containerId, environment ) );
    }


    @Override
    public void onEnvironmentDestroyed( final String environmentId )
    {
        LOG.info( String.format( "Environment destroyed: %s", environmentId ) );
    }
}
