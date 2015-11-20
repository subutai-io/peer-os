package io.subutai.core.peer.ui.container.clone;


import io.subutai.common.environment.ContainerType;
import io.subutai.common.peer.PeerException;
import io.subutai.common.protocol.Template;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;


public class CloneContainerTask implements Runnable
{
    private Cloner cloner;
    private LocalPeer localPeer;
    private ResourceHost resourceHost;
    private Template template;
    private String containerName;


    public CloneContainerTask( final Cloner cloner, final LocalPeer localPeer, final ResourceHost resourceHost,
                               final Template template, final String containerName )
    {
        this.cloner = cloner;
        this.localPeer = localPeer;
        this.resourceHost = resourceHost;
        this.template = template;
        this.containerName = containerName;
    }


    @Override
    public void run()
    {
//        try
//        {
//            cloner.updateContainerStatus( containerName, CloneResultType.START );
//
//            localPeer.createContainer( resourceHost, template, containerName,
//                    localPeer.getDefaultQuota( ContainerType.SMALL ) ).getHostname();
//            cloner.updateContainerStatus( containerName, CloneResultType.SUCCESS );
//        }
//        catch ( PeerException e )
//        {
//            cloner.updateContainerStatus( containerName, CloneResultType.FAIL );
//        }
    }
}
