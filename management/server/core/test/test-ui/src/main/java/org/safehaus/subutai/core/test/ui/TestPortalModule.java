package org.safehaus.subutai.core.test.ui;


import java.io.File;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.vaadin.ui.Component;


public class TestPortalModule implements PortalModule
{

    public static final String MODULE_IMAGE = "test.png";
    public static final String MODULE_NAME = "Test";

    public TestPortalModule(  )
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
        return new TestComponent( );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}
