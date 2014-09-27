package org.safehaus.subutai.common.protocol;


import java.util.UUID;


/**
 * Created by timur on 9/25/14.
 */
public class ExecuteCommandMessage extends PeerCommandMessage
{
    private String command;


    public ExecuteCommandMessage( UUID envId, UUID peerId, UUID agentId )
    {
        super( PeerCommandType.EXECUTE, envId, peerId, agentId );
    }


    @Override
    public void setResult( final Object result )
    {

    }


    @Override
    public Object getResult()
    {
        return null;
    }
}
