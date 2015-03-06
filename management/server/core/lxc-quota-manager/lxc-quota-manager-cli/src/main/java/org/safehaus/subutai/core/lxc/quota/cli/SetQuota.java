package org.safehaus.subutai.core.lxc.quota.cli;


import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.safehaus.subutai.common.quota.DiskQuotaUnit;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.core.lxc.quota.api.QuotaManager;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Created by talas on 12/8/14.
 */
@Command( scope = "quota", name = "set-quota", description = "Sets specified quota to container" )
public class SetQuota extends OsgiCommandSupport
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SetQuota.class );
    private QuotaManager quotaManager;
    private PeerManager peerManager;

    @Argument( index = 0, name = "container name", required = true, multiValued = false, description = "specify "
            + "container name" )
    private String containerName;

    @Argument( index = 1, name = "quota type", required = true, multiValued = false, description = "specify quota "
            + "type" )
    private String quotaType;

    @Argument( index = 2, name = "quota value", required = true, multiValued = false, description = "set quota value" )
    private String quotaValue;


    public SetQuota( QuotaManager quotaManager, final PeerManager peerManager )
    {
        this.quotaManager = quotaManager;
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        QuotaType quota = QuotaType.getQuotaType( quotaType );
        ContainerHost targetContainer = peerManager.getLocalPeer().getContainerHostByName( containerName );
        switch ( quota )
        {
            case QUOTA_TYPE_RAM:
                quotaManager.setRamQuota( targetContainer.getId(), Integer.valueOf( quotaValue ) );
                break;
            case QUOTA_TYPE_DISK_ROOTFS:
                quotaManager.setDiskQuota( targetContainer.getId(),
                        new DiskQuota( DiskPartition.ROOT_FS, DiskQuotaUnit.MB, Double.valueOf( quotaValue ) ) );
                break;
            case QUOTA_TYPE_DISK_HOME:
                quotaManager.setDiskQuota( targetContainer.getId(),
                        new DiskQuota( DiskPartition.HOME, DiskQuotaUnit.MB, Double.valueOf( quotaValue ) ) );
                break;
            case QUOTA_TYPE_DISK_OPT:
                quotaManager.setDiskQuota( targetContainer.getId(),
                        new DiskQuota( DiskPartition.OPT, DiskQuotaUnit.MB, Double.valueOf( quotaValue ) ) );
                break;
            case QUOTA_TYPE_DISK_VAR:
                quotaManager.setDiskQuota( targetContainer.getId(),
                        new DiskQuota( DiskPartition.VAR, DiskQuotaUnit.MB, Double.valueOf( quotaValue ) ) );
                break;
            case QUOTA_TYPE_CPU:
                quotaManager.setCpuQuota( targetContainer.getId(), Integer.valueOf( quotaValue ) );
                break;
        }
        return null;
    }
}
