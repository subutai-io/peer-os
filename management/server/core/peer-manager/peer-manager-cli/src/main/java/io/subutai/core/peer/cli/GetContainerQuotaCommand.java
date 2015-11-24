package io.subutai.core.peer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.environment.Environment;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.quota.QuotaType;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "peer", name = "get-quota", description = "gets quota information from peer for container" )
public class GetContainerQuotaCommand extends SubutaiShellCommandSupport
{
    @Argument( index = 0, name = "peer id", multiValued = false, required = true, description = "Id of target peer" )
    private String peerId;

    @Argument( index = 1, name = "environment id", multiValued = false, required = true, description = "Id of "
            + "environment" )
    protected String environmentId;

    @Argument( index = 2, name = "container name", multiValued = false, required = true, description = "container "
            + "name" )
    protected String containerName;

    @Argument( index = 3, name = "quota type", multiValued = false, required = true, description = "quota type "
            + "(quota:list-quota to list all quotas)" )
    protected String quotaType;

    private PeerManager peerManager;
    private EnvironmentManager environmentManager;


    public GetContainerQuotaCommand( PeerManager peerManager, EnvironmentManager environmentManager )
    {
        this.peerManager = peerManager;
        this.environmentManager = environmentManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {


        Peer peer = peerManager.getPeer( peerId );
        Environment environment = environmentManager.loadEnvironment( environmentId );

        ContainerHost targetContainer = environment.getContainerHostByHostname( containerName );
        if ( targetContainer == null )
        {
            System.out.println( "Couldn't get container host for name: " + containerName );
        }
        else
        {
            //TODO: check or implement this
//            System.out.println( peer.getQuotaInfo( targetContainer, QuotaType.getQuotaType( quotaType ) ) );
        }
        return null;
    }
}
