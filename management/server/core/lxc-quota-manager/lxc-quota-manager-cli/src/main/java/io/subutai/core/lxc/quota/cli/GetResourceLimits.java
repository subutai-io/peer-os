package io.subutai.core.lxc.quota.cli;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.resource.HostResources;
import io.subutai.common.resource.PeerResources;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.lxc.quota.api.QuotaManager;


@Command( scope = "quota", name = "limits", description = "Gets limits for local peer" )
public class GetResourceLimits extends SubutaiShellCommandSupport
{
    private static final Logger LOGGER = LoggerFactory.getLogger( GetResourceLimits.class );

    private LocalPeer localPeer;
    private QuotaManager quotaManager;


    public GetResourceLimits( QuotaManager quotaManager, LocalPeer localPeer )
    {
        this.quotaManager = quotaManager;
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute()
    {
        final PeerResources limits = quotaManager.getResourceLimits( localPeer.getId() );

        System.out.println(
                String.format( "%s, env:%d, cont:%d, net: %d", limits.getPeerId(), limits.getEnvironmentLimit(),
                        limits.getContainerLimit(), limits.getNetworkLimit() ) );

        for ( HostResources resources : limits.getHostResources() )
        {
            System.out.println( String.format( "\t%s, cpu: %s, ram:%s, disk:%s", resources.getHostId(),
                    resources.getCpuLimit().getPrintValue(), resources.getRamLimit().getPrintValue(),
                    resources.getDiskLimit().getPrintValue() ) );
        }
        return null;
    }
}
