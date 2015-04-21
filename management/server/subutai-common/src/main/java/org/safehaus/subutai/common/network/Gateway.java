package org.safehaus.subutai.common.network;


public class Gateway
{
    private int vlan;
    private String ip;


    public Gateway( final int vlan, final String ip )
    {
        this.vlan = vlan;
        this.ip = ip;
    }


    public int getVlan()
    {
        return vlan;
    }


    public String getIp()
    {
        return ip;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof Gateway ) )
        {
            return false;
        }

        final Gateway gateway = ( Gateway ) o;

        if ( vlan != gateway.getVlan() )
        {
            return false;
        }
        if ( ip != null ? !ip.equals( gateway.getIp() ) : gateway.getIp() != null )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        int result = vlan;
        result = 31 * result + ( ip != null ? ip.hashCode() : 0 );
        return result;
    }
}
