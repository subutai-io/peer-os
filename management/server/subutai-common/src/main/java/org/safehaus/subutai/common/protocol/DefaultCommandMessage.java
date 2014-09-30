package org.safehaus.subutai.common.protocol;


import java.util.UUID;


/**
 * Created by bahadyr on 9/26/14.
 */
public class DefaultCommandMessage extends PeerCommandMessage
{
    private String result;

    public DefaultCommandMessage( PeerCommandType type, UUID envId, UUID peerId, UUID agentId )
    {
        super( type, envId, peerId, agentId );
    }


    @Override
    public void setResult( final Object result )
    {

        if ( !( result instanceof String ) )
        {
            throw new IllegalArgumentException( "Argument must be String." );
        }

        this.result = ( String ) result;
    }


    @Override
    public String getResult()
    {
        return result;
    }
}
