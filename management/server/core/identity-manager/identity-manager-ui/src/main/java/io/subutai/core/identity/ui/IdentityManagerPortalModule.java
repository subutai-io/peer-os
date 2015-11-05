package io.subutai.core.identity.ui;


import java.io.File;
import io.subutai.common.util.FileUtil;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.server.ui.api.PortalModule;
import com.vaadin.ui.Component;

public class IdentityManagerPortalModule implements PortalModule
{

    private final static String MODULE_IMAGE = "identity.png";
    private final static String MODULE_NAME = "Identity Manager";
    private IdentityManager identityManager;


    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
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
        return new IdentityManagerComponent( this, identityManager );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}