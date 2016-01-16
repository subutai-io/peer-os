package io.subutai.core.lxc.quota.cli;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.resource.ContainerResourceType;
import io.subutai.common.resource.ResourceValueParser;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.lxc.quota.api.QuotaManager;


@Command( scope = "quota", name = "set", description = "Sets specified quota to container" )
public class SetQuota extends SubutaiShellCommandSupport
{
    private static final Logger LOGGER = LoggerFactory.getLogger( SetQuota.class );
    private final LocalPeer localPeer;
    private QuotaManager quotaManager;

    @Argument( index = 0, name = "container name", required = true, multiValued = false, description = "specify "
            + "container name" )
    private String containerName;

    @Argument( index = 1, name = "resource type", required = true, multiValued = false, description = "specify resource "
            + "type" )
    private String resourceType;

    @Argument( index = 2, name = "quota value", required = true, multiValued = false, description = "set quota value" )
    private String quotaValue;


    public SetQuota( QuotaManager quotaManager, LocalPeer localPeer )
    {
        this.quotaManager = quotaManager;
        this.localPeer = localPeer;
    }


    public void setContainerName( final String containerName )
    {
        this.containerName = containerName;
    }


    public void setResourceType( final String resourceType )
    {
        this.resourceType = resourceType;
    }


    public void setQuotaValue( final String quotaValue )
    {
        this.quotaValue = quotaValue;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        ContainerResourceType type = ContainerResourceType.valueOf( resourceType );
        ContainerHost containerHost = localPeer.getContainerHostByName( containerName );

        ResourceValueParser parser = quotaManager.getResourceValueParser( type );
//        ByteResourceValue resourceValue = parser.parse( quotaValue );
//        quotaManager.setQuota( containerHost.getContainerId(), type, resourceValue );
        return null;
    }
}
