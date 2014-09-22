package org.safehaus.subutai.core.apt.cli;


import java.util.List;

import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.apt.api.AptRepoException;
import org.safehaus.subutai.core.apt.api.AptRepositoryManager;
import org.safehaus.subutai.core.apt.api.PackageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command(scope = "apt", name = "list-packages", description = "List packages in apt repository by pattern")
public class ListPackagesCommand extends OsgiCommandSupport
{
    private static final Logger LOG = LoggerFactory.getLogger( ListPackagesCommand.class.getName() );


    @Argument(index = 0, name = "pattern", required = true, multiValued = false, description = "search pattern")
    String pattern;

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
            List<PackageInfo> packageInfoList = aptRepositoryManager
                    .listPackages( agentManager.getAgentByHostname( Common.MANAGEMENT_AGENT_HOSTNAME ), pattern );
            for ( PackageInfo packageInfo : packageInfoList )
            {
                System.out.println( packageInfo.toString() );
            }
        }
        catch ( AptRepoException e )
        {
            LOG.error( "Error in doExecute", e );
        }
        return null;
    }
}
