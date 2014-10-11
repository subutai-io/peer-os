package org.safehaus.subutai.common.protocol;


import java.lang.reflect.Type;
import java.util.Set;
import java.util.UUID;

import com.google.gson.reflect.TypeToken;


/**
 * Created by bahadyr on 9/19/14.
 */
public class DestroyContainersMessage extends PeerCommandMessage
{

    String hostname;


    public String getHostname()
    {
        return hostname;
    }


    public void setHostname( final String hostname )
    {
        this.hostname = hostname;
    }


    public DestroyContainersMessage( final PeerCommandType type, final UUID envId, final UUID peerId,
                                     final UUID agentId )
    {
        super( type, envId, peerId, agentId );
    }


    @Override
    public Type getResultObjectType()
    {
        return new TypeToken<Set<Agent>>()
        {
        }.getType();
    }
}
