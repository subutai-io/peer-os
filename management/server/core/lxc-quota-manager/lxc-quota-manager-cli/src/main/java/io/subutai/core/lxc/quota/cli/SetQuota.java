package io.subutai.core.lxc.quota.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.hub.share.quota.ContainerQuota;
import io.subutai.hub.share.quota.ContainerResource;
import io.subutai.hub.share.quota.ContainerResourceFactory;
import io.subutai.hub.share.quota.Quota;
import io.subutai.hub.share.resource.ContainerResourceType;
import io.subutai.hub.share.resource.ResourceValue;
import io.subutai.hub.share.resource.ResourceValueParser;


@Command( scope = "quota", name = "set", description = "Sets specified quota to container" )
public class SetQuota extends SubutaiShellCommandSupport
{
    private final LocalPeer localPeer;
    private QuotaManager quotaManager;

    @Argument( name = "container name", required = true, description = "specify " + "container name" )
    private String containerName;

    @Argument( index = 1, name = "resource type", required = true, description = "specify resource " + "type" )
    private String resourceType;

    @Argument( index = 2, name = "quota value", required = true, description = "set quota value" )
    private String quotaValue;

    @Argument( index = 3, name = "quota threshold", required = true, description = "set quota " + "threshold" )
    private Integer threshold;


    public SetQuota( QuotaManager quotaManager, LocalPeer localPeer )
    {
        this.quotaManager = quotaManager;
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        ContainerResourceType type = ContainerResourceType.parse( resourceType );

        ContainerQuota containerQuota = new ContainerQuota();

        final ResourceValueParser parser = quotaManager.getResourceValueParser( type );

        final ResourceValue value = parser.parse( quotaValue );

        ContainerResource containerResource = getContainerResource( type, value );

        containerQuota.add( new Quota( containerResource, threshold ) );
        final ContainerHost container = localPeer.getContainerHostByContainerName( containerName );
        if ( container == null )
        {
            System.out.println( "Container not found by id." );
        }
        else
        {
            final ContainerId id = container.getContainerId();
            quotaManager.setQuota( id, containerQuota );
        }
        return null;
    }


    private ContainerResource getContainerResource( ContainerResourceType type, ResourceValue value )
    {
        return ContainerResourceFactory.createContainerResource( type, value );
    }
}
