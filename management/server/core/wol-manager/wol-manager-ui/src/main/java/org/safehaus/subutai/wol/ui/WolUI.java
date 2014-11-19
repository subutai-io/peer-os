package org.safehaus.subutai.wol.ui;


import com.vaadin.ui.Component;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.wol.api.WolManager;
import java.io.File;


/**
 * Created by emin on 14/11/14.
 */
public class WolUI implements PortalModule {

    public static final String MODULE_IMAGE = "wol.png";
    public static final String MODULE_NAME = "WakeOnLan";
    private WolManager wolManager;


    public WolManager getWolManager()
    {
        return wolManager;
    }


    public void setWolManager( final WolManager wolManager )
    {
        this.wolManager = wolManager;
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
        return new WolForm(this);
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}
