package io.subutai.core.lxc.quota.cli;


import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.QuotaException;
import io.subutai.common.quota.QuotaType;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.core.peer.api.PeerManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;


@Command( scope = "quota", name = "get", description = "Gets quota for specified container" )
public class GetQuota extends SubutaiShellCommandSupport
{
    private static final Logger LOGGER = LoggerFactory.getLogger( GetQuota.class );
    @Argument( index = 0, name = "container name", required = true, multiValued = false,
            description = "container name" )
    private String containerName;

    @Argument( index = 1, name = "quota type", required = true, multiValued = false,
            description = "quota type to get specific quota" )
    private String quotaType;

    private QuotaManager quotaManager;


    public GetQuota( QuotaManager quotaManager )
    {
        this.quotaManager = quotaManager;
    }


    public void setContainerName( final String containerName )
    {
        this.containerName = containerName;
    }


    public void setQuotaType( final String quotaType )
    {
        this.quotaType = quotaType;
    }


    @Override
    protected Object doExecute()
    {
        try
        {
            QuotaType quotaType = QuotaType.getQuotaType( this.quotaType );

            System.out.println( quotaManager.getQuota( containerName, quotaType ) );
        }
        catch ( QuotaException e )
        {
            System.out.println( "Error getting quota for container" );
            LOGGER.error( "Error getting quota for container", e );
        }
        return null;
    }
}
