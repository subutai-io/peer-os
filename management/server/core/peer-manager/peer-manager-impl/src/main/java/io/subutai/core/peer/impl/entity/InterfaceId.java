package io.subutai.core.peer.impl.entity;


import java.io.Serializable;


public class InterfaceId implements Serializable
{
    String ip;

    String mac;


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof InterfaceId ) )
        {
            return false;
        }

        final InterfaceId that = ( InterfaceId ) o;

        if ( !ip.equals( that.ip ) )
        {
            return false;
        }
        return mac.equals( that.mac );
    }


    @Override
    public int hashCode()
    {
        int result = ip.hashCode();
        result = 31 * result + mac.hashCode();
        return result;
    }
}
