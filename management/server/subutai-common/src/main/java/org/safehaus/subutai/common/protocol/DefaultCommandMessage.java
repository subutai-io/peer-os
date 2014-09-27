package org.safehaus.subutai.common.protocol;


import java.util.UUID;


/**
 * Created by bahadyr on 9/26/14.
 */
public class DefaultCommandMessage extends PeerCommandMessage

{
    public DefaultCommandMessage( PeerCommandType type, UUID envId, UUID peerId, UUID agentId )
    {
        super( type, envId, peerId, agentId );
    }


    @Override
    public void setResult( final Object result )
    {

        if ( !( result instanceof Boolean ) )
        {
            throw new IllegalArgumentException( "Argument must be boolean." );
        }

        this.success = ( Boolean ) result;
    }


    @Override
    public Boolean getResult()
    {
        return success;
    }
}
