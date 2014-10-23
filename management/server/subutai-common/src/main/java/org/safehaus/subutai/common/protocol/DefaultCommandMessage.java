package org.safehaus.subutai.common.protocol;


import java.lang.reflect.Type;
import java.util.UUID;

import com.google.gson.reflect.TypeToken;


/**
 * Created by bahadyr on 9/26/14.
 */
public class DefaultCommandMessage extends PeerCommandMessage
{

    public DefaultCommandMessage( PeerCommandType type, /*UUID envId, */UUID peerId, UUID agentId )
    {
        super( type,/* envId,*/ peerId, agentId );
    }


    @Override
    public Type getResultObjectType()
    {
        return new TypeToken<String>()
        {
        }.getType();
    }


    @Override
    public Type getInputObjectType()
    {
        return new TypeToken<String>()
        {
        }.getType();
    }
}
