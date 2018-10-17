package io.subutai.core.environment.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.environment.Environment;
import io.subutai.common.peer.ContainerHost;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.bazaar.share.quota.ContainerQuota;
import io.subutai.bazaar.share.quota.Quota;


@Command( scope = "environment", name = "get-quota", description = "gets quota information from peer for container" )
public class GetContainerQuotaCommand extends SubutaiShellCommandSupport
{

    @Argument( index = 0, name = "environment id", multiValued = false, required = true, description = "Id of "
            + "environment" )
    protected String environmentId;

    @Argument( index = 1, name = "container id", multiValued = false, required = true, description = "container "
            + "id" )
    protected String containerId;

    private EnvironmentManager environmentManager;


    public GetContainerQuotaCommand( EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Environment environment = environmentManager.loadEnvironment( environmentId );

        ContainerHost targetContainer = environment.getContainerHostById( containerId );
        if ( targetContainer == null )
        {
            System.out.println( "Couldn't get container host by name: " + containerId );
        }
        else
        {
            final ContainerQuota containerQuota = targetContainer.getQuota();
            for ( Quota quota : containerQuota.getAll() )
            {
                System.out.println( String.format( "%s\t%s", quota.getResource().getContainerResourceType(),
                        quota.getResource().getPrintValue() ) );
            }
        }
        return null;
    }
}
