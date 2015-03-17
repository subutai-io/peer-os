package org.safehaus.subutai.core.peer.cli;


import java.util.UUID;

import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;


/**
 * Created by talas on 12/12/14.
 */
@Command( scope = "peer", name = "get-quota", description = "gets quota information from peer for container" )
public class GetContainerQuotaCommand extends SubutaiShellCommandSupport
{
    @Argument( index = 0, name = "peer id", multiValued = false, required = true, description = "Id of target peer" )
    private String peerId;

    @Argument( index = 1, name = "environment id", multiValued = false, required = true, description = "Id of "
            + "environment" )
    private String environmentId;

    @Argument( index = 2, name = "container name", multiValued = false, required = true, description = "container "
            + "name" )
    private String containerName;

    @Argument( index = 3, name = "quota type", multiValued = false, required = true, description = "quota type "
            + "(quota:list-quota to list all quotas)" )
    private String quotaType;

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
        Environment environment = environmentManager.findEnvironment( UUID.fromString( environmentId ) );

        ContainerHost targetContainer = environment.getContainerHostByHostname( containerName );
        if ( targetContainer == null )
        {
            System.out.println( "Couldn't get container host for name: " + containerName );
        }
        else
        {
            System.out.println( peer.getQuota( targetContainer, QuotaType.getQuotaType( quotaType ) ) );
        }
        return null;
    }
}
