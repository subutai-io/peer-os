package org.safehaus.subutai.core.peer.ui.container.executor;


import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ResourceHost;


public class CloneCommand implements AgentCommand
{
    private LocalPeer localPeer;
    private ResourceHost resourceHost;
    private Template template;
    private String containerName;


    public CloneCommand( LocalPeer localPeer, ResourceHost resourceHost, Template template, String containerName )
    {
        this.localPeer = localPeer;
        this.resourceHost = resourceHost;
        this.template = template;
        this.containerName = containerName;
    }


    @Override
    public void execute() throws AgentExecutionException
    {
        try
        {
            localPeer.createContainer( resourceHost, template, containerName );
        }
        catch ( PeerException e )
        {
            throw new AgentExecutionException( e.getMessage() );
        }
    }
}
