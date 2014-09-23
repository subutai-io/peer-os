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


@Command( scope = "apt", name = "add-package", description = "Add package to apt repository by path" )
public class AddPackageCommand extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( AddPackageCommand.class.getName() );

    @Argument( index = 0, name = "package path", required = true, multiValued = false,
            description = "path to package" )
    String packagePath;

    private AptRepositoryManager aptRepositoryManager;
    private AgentManager agentManager;


    public void setAptRepositoryManager( final AptRepositoryManager aptRepositoryManager )
    {
        this.aptRepositoryManager = aptRepositoryManager;
    }


    public void setAgentManager( final AgentManager agentManager )
    {
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
        }
        return null;
    }
}
