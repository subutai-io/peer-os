package io.subutai.core.executor.ui;


import java.io.File;

import org.safehaus.subutai.common.util.FileUtil;
import io.subutai.core.executor.api.CommandExecutor;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.google.common.base.Preconditions;
import com.vaadin.ui.Component;


/**
 * Terminal module
 */
public class CommandExecutorModule implements PortalModule
{
    public static final String MODULE_IMAGE = "terminal.png";
    public static final String MODULE_NAME = "Terminal";
    private final CommandExecutor commandExecutor;
    private final HostRegistry hostRegistry;


    public CommandExecutorModule( final CommandExecutor commandExecutor, final HostRegistry hostRegistry )
    {
        Preconditions.checkNotNull( commandExecutor );
        Preconditions.checkNotNull( hostRegistry );

        this.commandExecutor = commandExecutor;
        this.hostRegistry = hostRegistry;
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
        return new TerminalForm( commandExecutor, hostRegistry );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}
