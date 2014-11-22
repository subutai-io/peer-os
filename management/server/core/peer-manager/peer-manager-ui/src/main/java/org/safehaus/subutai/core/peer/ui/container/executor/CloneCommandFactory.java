package org.safehaus.subutai.core.peer.ui.container.executor;


import java.util.UUID;

import org.safehaus.subutai.core.peer.api.LocalPeer;


/**
 * Created by timur on 9/8/14.
 */
public class CloneCommandFactory implements AgentCommandFactory
{
    //    private ContainerManager containerManager;
    private LocalPeer localPeer;
    private String hostName;
    private String templateName;
    private UUID envId;


    public CloneCommandFactory( LocalPeer localPeer, UUID envId, String hostname, String templateName )
    {
        //        this.containerManager = containerManager;
        this.localPeer = localPeer;
        this.hostName = hostname;
        this.templateName = templateName;
        this.envId = envId;
    }


    @Override
    public AgentCommand newCommand( String cloneName )
    {
        return new CloneCommand( localPeer, hostName, templateName, cloneName, envId );
    }
}
