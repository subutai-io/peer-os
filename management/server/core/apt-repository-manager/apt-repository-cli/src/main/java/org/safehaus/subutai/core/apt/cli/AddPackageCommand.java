package org.safehaus.subutai.core.apt.cli;


import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.apt.api.AptRepoException;
import org.safehaus.subutai.core.apt.api.AptRepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


@Command(scope = "apt", name = "add-package", description = "Add package to apt repository by path")
public class AddPackageCommand extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( AddPackageCommand.class.getName() );

    @Argument(index = 0, name = "package path", required = true, multiValued = false,
            description = "absolute path to package")
    String packagePath;

    private final AptRepositoryManager aptRepositoryManager;
    private final AgentManager agentManager;


    public AddPackageCommand( final AptRepositoryManager aptRepositoryManager, final AgentManager agentManager )
    {

        Preconditions.checkNotNull( aptRepositoryManager, "Apt Repo Manager is null" );
        Preconditions.checkNotNull( agentManager, "Agent Manager is null" );

        this.aptRepositoryManager = aptRepositoryManager;
        this.agentManager = agentManager;
    }


    @Override
    protected Object doExecute()
    {

        try
        {
            aptRepositoryManager
                    .addPackageByPath( agentManager.getAgentByHostname( Common.MANAGEMENT_AGENT_HOSTNAME ), packagePath,
                            false );
        }
        catch ( AptRepoException e )
        {
            LOG.error( "Error in doExecute", e );
            System.out.println( e.getMessage() );
        }

        return null;
    }
}
