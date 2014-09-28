package org.safehaus.subutai.common.protocol;


import java.util.UUID;


/**
 * Created by bahadyr on 9/26/14.
 */
public class DefaultCommandMessage extends PeerCommandMessage

{
    protected Boolean result;


    public DefaultCommandMessage( PeerCommandType type, UUID peerId, UUID agentId )
    {
        super( type, peerId, agentId );
    }


    @Override
    public void setResult( final Object result )
    {
        if ( result != null && result instanceof Boolean )
        {
            this.result = ( Boolean ) result;
        }
    }


    @Override
    public Object getResult()
    {
        return result;
    }
}
