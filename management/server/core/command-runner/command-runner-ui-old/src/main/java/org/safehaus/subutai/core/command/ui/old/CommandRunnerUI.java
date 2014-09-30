package org.safehaus.subutai.core.command.ui.old;


import java.io.File;

import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;
import org.safehaus.subutai.server.ui.api.PortalModule;

import com.google.common.base.Preconditions;
import com.vaadin.ui.Component;


public class CommandRunnerUI implements PortalModule
{

    public static final String MODULE_IMAGE = "terminal.png";
    public static final String MODULE_NAME = "Terminal Old";
    private final CommandDispatcher commandRunner;
    private final AgentManager agentManager;


    public CommandRunnerUI( final CommandDispatcher commandRunner, final AgentManager agentManager )
    {
        Preconditions.checkNotNull( commandRunner, "Command Runner is null" );
        Preconditions.checkNotNull( agentManager, "Agent Manager is null" );

        this.commandRunner = commandRunner;
        this.agentManager = agentManager;
    }


    @Override
    public String getId()
    {
        return CommandRunnerUI.MODULE_NAME;
    }


    @Override
    public String getName()
    {
        return CommandRunnerUI.MODULE_NAME;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( CommandRunnerUI.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        return new TerminalForm( commandRunner, agentManager );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }
}
