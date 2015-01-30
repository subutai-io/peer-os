package org.safehaus.subutai.core.peer.ui.container.executor;


import java.util.UUID;

import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.core.peer.api.LocalPeer;


/**
 * Created by timur on 9/8/14.
 */
public class CloneCommand implements AgentCommand
{
    //    private ContainerManager containerManager;
    private LocalPeer localPeer;
    private String hostName;
    private String templateName;
    private String cloneName;
    private UUID envId;


    public CloneCommand( LocalPeer localPeer, String hostName, String templateName, String cloneName, UUID envId )
    {
        this.localPeer = localPeer;
        this.hostName = hostName;
        this.templateName = templateName;
        this.cloneName = cloneName;
        this.envId = envId;
    }


    @Override
    public void execute() throws AgentExecutionException
    {
        try
        {
            localPeer.createContainer( hostName, templateName, cloneName, envId );
            //            containerManager.clone( envId, hostName, templateName, cloneName );
        }
        catch ( PeerException e )
        {
            throw new AgentExecutionException( e.getMessage() );
        }
    }
}
