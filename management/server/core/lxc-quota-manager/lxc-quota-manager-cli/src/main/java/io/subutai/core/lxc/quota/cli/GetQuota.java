package io.subutai.core.lxc.quota.cli;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.quota.QuotaException;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.lxc.quota.api.QuotaManager;


@Command( scope = "quota", name = "get", description = "Gets quota for specified container" )
public class GetQuota extends SubutaiShellCommandSupport
{
    private static final Logger LOGGER = LoggerFactory.getLogger( GetQuota.class );
    @Argument( index = 0, name = "container name", required = true, multiValued = false,
            description = "container name" )
    private String containerName;


    private QuotaManager quotaManager;
    private LocalPeer localPeer;


    public GetQuota( QuotaManager quotaManager, LocalPeer localPeer )
    {
        this.quotaManager = quotaManager;
        this.localPeer = localPeer;
    }


    public void setContainerName( final String containerName )
    {
        this.containerName = containerName;
    }


    @Override
    protected Object doExecute()
    {
        try
        {
            ContainerHost containerHost = localPeer.getContainerHostByName( containerName );
            System.out.println( quotaManager.getQuota( containerHost.getContainerId() ) );
        }
        catch ( HostNotFoundException | QuotaException e )
        {
            System.out.println( "Error getting quota for container" );
            LOGGER.error( "Error getting quota for container", e );
        }
        return null;
    }
}
