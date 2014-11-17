package org.safehaus.subutai.core.peer.api;


public class PeerEvent<T>
{
    PeerEventType type;
    T object;


    public PeerEvent( PeerEventType type, T object )
    {
        this.type = type;
        this.object = object;
    }


    public PeerEventType getType()
    {
        return type;
    }


    public void setType( final PeerEventType type )
    {
        this.type = type;
    }


    public T getObject()
    {
        return object;
    }


    public void setObject( final T object )
    {
        this.object = object;
    }
}
