package org.safehaus.subutai.core.channel.ui;


import java.io.File;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.channel.api.ChannelManager;
import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


/**
 * Created by ermek on 3/4/15.
 */
public class ChannelManagerPortalModule implements PortalModule
{
    private final static String MODULE_IMAGE = "chanel.png";
    private final static String MODULE_NAME = "Channel (Tunnel) Manager";
    private ChannelManager channelManager;
    private IdentityManager identityManager;


    public ChannelManagerPortalModule( ChannelManager channelManager , IdentityManager identityManager)
    {
        this.channelManager  = channelManager;
        this.identityManager = identityManager;
    }


    private void setChannelManager( final ChannelManager channelManager )
    {
        this.channelManager = channelManager;
    }


    public void init()
    {
        // empty method
    }


    public void destroy()
    {
        // empty method
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
        return new ChannelManagerComponent( this, channelManager, identityManager  );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}