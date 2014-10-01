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


@Command(scope = "apt", name = "remove-package", description = "Remove package from apt repository by name")
public class RemovePackageCommand extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( RemovePackageCommand.class.getName() );

    @Argument(index = 0, name = "package name", required = true, multiValued = false, description = "name of package")
    String packageName;

    private final AptRepositoryManager aptRepositoryManager;
    private final AgentManager agentManager;


    public RemovePackageCommand( final AptRepositoryManager aptRepositoryManager, final AgentManager agentManager )
    {
        Preconditions.checkNotNull( aptRepositoryManager, "Apt Repo Manager is null" );
        Preconditions.checkNotNull( agentManager, "Agent Manager is null" );

        this.aptRepositoryManager = aptRepositoryManager;
        this.agentManager = agentManager;
    }


    public void setPackageName( final String packageName )
    {
        this.packageName = packageName;
    }


    @Override
    protected Object doExecute()
    {

        try
        {
            aptRepositoryManager
                    .removePackageByName( agentManager.getAgentByHostname( Common.MANAGEMENT_AGENT_HOSTNAME ),
                            packageName );
        }
        catch ( AptRepoException e )
        {
            LOG.error( "Error in doExecute", e );
            System.out.println( e.getMessage() );
        }

        return null;
    }
}
