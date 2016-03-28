package io.subutai.common.network;


import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;


public class Vnis
{
    @JsonProperty( "vnis" )
    private Set<Vni> vnis = new HashSet<>();


    public Vnis( final Set<Vni> vnis )
    {
        this.vnis = vnis;
    }


    public Vnis()
    {
    }


    public Set<Vni> list()
    {
        return vnis;
    }


    public void add( final Vni vni )
    {
        if ( vni == null )
        {
            throw new IllegalArgumentException( "VNI could not be null." );
        }
        this.vnis.add( vni );
    }


    public Integer findVlanByVni( long vni ) throws PeerException
    {
        for ( Vni reservedVni : this.vnis )
        {
            if ( reservedVni.getVni() == vni )
            {
                return reservedVni.getVlan();
            }
        }

        return null;
    }


    public Vni findVniByEnvironmentId( final String environmentId )
    {
        //check if vni is already reserved
        for ( Vni aVni : this.vnis )
        {
            if ( aVni.getEnvironmentId().equalsIgnoreCase( environmentId ) )
            {
                return aVni;
            }
        }

        return null;
    }


    public int findAvailableVlanId() throws PeerException
    {
        for ( int i = Common.MIN_VLAN_ID; i <= Common.MAX_VLAN_ID; i++ )
        {
            if ( findVniByVlanId( i ) == null )
            {
                return i;
            }
        }

        throw new PeerException( "No available vlan found" );
    }


    public Vni findVniByVlanId( final int vlanId )
    {
        for ( Vni vni : this.vnis )
        {
            if ( vni.getVlan() == vlanId )
            {
                return vni;
            }
        }
        return null;
    }
}
