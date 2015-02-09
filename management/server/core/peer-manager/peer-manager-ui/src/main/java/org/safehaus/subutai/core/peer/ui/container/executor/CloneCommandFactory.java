package org.safehaus.subutai.core.peer.ui.container.executor;


import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ResourceHost;


public class CloneCommandFactory implements AgentCommandFactory
{
    private LocalPeer localPeer;
    private ResourceHost resourceHost;
    private Template template;


    public CloneCommandFactory( LocalPeer localPeer, ResourceHost resourceHost, Template template )
    {
        this.localPeer = localPeer;
        this.resourceHost = resourceHost;
        this.template = template;
    }


    @Override
    public AgentCommand newCommand( String containerName )
    {
        return new CloneCommand( localPeer, resourceHost, template, containerName );
    }
}
