package io.subutai.core.lxc.quota.cli;


import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.QuotaException;
import org.safehaus.subutai.common.quota.QuotaType;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.peer.api.HostNotFoundException;
import io.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;


@Command( scope = "quota", name = "get-quota", description = "Gets quota for specified container" )
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
    private PeerManager peerManager;


    public GetQuota( QuotaManager quotaManager, final PeerManager peerManager )
    {
        this.quotaManager = quotaManager;
        this.peerManager = peerManager;
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
            QuotaType quota = QuotaType.getQuotaType( quotaType );
            ContainerHost targetContainer = peerManager.getLocalPeer().getContainerHostByName( containerName );
            switch ( quota )
            {
                case QUOTA_TYPE_RAM:
                    System.out.println( quotaManager.getRamQuota( targetContainer.getId() ) );
                    break;
                case QUOTA_TYPE_DISK_ROOTFS:
                    System.out.println( quotaManager.getDiskQuota( targetContainer.getId(), DiskPartition.ROOT_FS ) );
                    break;
                case QUOTA_TYPE_DISK_HOME:
                    System.out.println( quotaManager.getDiskQuota( targetContainer.getId(), DiskPartition.HOME ) );
                    break;
                case QUOTA_TYPE_DISK_OPT:
                    System.out.println( quotaManager.getDiskQuota( targetContainer.getId(), DiskPartition.OPT ) );
                    break;
                case QUOTA_TYPE_DISK_VAR:
                    System.out.println( quotaManager.getDiskQuota( targetContainer.getId(), DiskPartition.VAR ) );
                    break;
                case QUOTA_TYPE_CPU:
                    System.out.println( quotaManager.getCpuQuota( targetContainer.getId() ) );
                    break;
            }
        }
        catch ( QuotaException | HostNotFoundException e )
        {
            System.out.println( "Error getting quota for container" );
            LOGGER.error( "Error getting quota for container", e );
        }
        return null;
    }
}
