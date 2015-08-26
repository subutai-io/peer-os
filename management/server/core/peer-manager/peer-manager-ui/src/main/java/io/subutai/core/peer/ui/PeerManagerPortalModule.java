package io.subutai.core.peer.ui;


import java.io.File;

import io.subutai.common.util.FileUtil;
import io.subutai.core.env.api.EnvironmentManager;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registry.api.TemplateRegistry;
import io.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class PeerManagerPortalModule implements PortalModule
{

    public static final String MODULE_IMAGE = "peer.png";
    public static final String MODULE_NAME = "Peer";
    private PeerManager peerManager;
    private TemplateRegistry registry;
    private HostRegistry hostRegistry;
    private EnvironmentManager environmentManager;


    public EnvironmentManager getEnvironmentManager()
    {
        return environmentManager;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public TemplateRegistry getRegistry()
    {
        return registry;
    }


    public HostRegistry getHostRegistry()
    {
        return hostRegistry;
    }


    public PeerManagerPortalModule( final PeerManager peerManager, final TemplateRegistry registry,
                                    final HostRegistry hostRegistry )
    {
        this.peerManager = peerManager;
        this.registry = registry;
        this.hostRegistry = hostRegistry;
    }


    public void init()
    {

    }


    public void destroy()
    {
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
        return new PeerComponent( this );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}
