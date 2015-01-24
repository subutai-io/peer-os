package org.safehaus.subutai.core.env.ui;


import java.io.File;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class EnvironmentManagerPortalModule implements PortalModule
{

    private final static String MODULE_IMAGE = "environment.jpg";
    private final static String MODULE_NAME = "Environment2";
    private EnvironmentManager environmentManager;
    private PeerManager peerManager;


    public EnvironmentManagerPortalModule( final EnvironmentManager environmentManager, final PeerManager peerManager )
    {
        this.environmentManager = environmentManager;
        this.peerManager = peerManager;
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
        return new EnvironmentManagerComponent( environmentManager, peerManager );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}
