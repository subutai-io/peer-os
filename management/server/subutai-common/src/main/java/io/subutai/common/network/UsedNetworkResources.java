package io.subutai.common.network;


import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;

import io.subutai.common.settings.Common;
import io.subutai.common.util.IPUtil;
import io.subutai.common.util.NumUtil;


public class UsedNetworkResources
{
    private static final long VLAN_CACHING_INTERVAL_SEC = 60;
    private static Cache<Integer, Boolean> cachedVlans = CacheBuilder.newBuilder().
            expireAfterWrite( VLAN_CACHING_INTERVAL_SEC, TimeUnit.SECONDS ).build();

    @JsonProperty( "vnis" )
    private Set<Long> vnis = Sets.newConcurrentHashSet();
    @JsonProperty( "p2pSubnets" )
    private Set<String> p2pSubnets = Sets.newConcurrentHashSet();
    @JsonProperty( "containerSubnets" )
    private Set<String> containerSubnets = Sets.newConcurrentHashSet();
    @JsonProperty( "vlans" )
    private Set<Integer> vlans = Sets.newConcurrentHashSet();


    public UsedNetworkResources( @JsonProperty( "vnis" ) final Set<Long> vnis,
                                 @JsonProperty( "p2pSubnets" ) final Set<String> p2pSubnets,
                                 @JsonProperty( "containerSubnets" ) final Set<String> containerSubnets,
                                 @JsonProperty( "vlans" ) final Set<Integer> vlans )
    {
        Preconditions.checkNotNull( vnis );
        Preconditions.checkNotNull( p2pSubnets );
        Preconditions.checkNotNull( containerSubnets );
        Preconditions.checkNotNull( vlans );

        this.vnis = vnis;
        this.p2pSubnets = p2pSubnets;
        this.containerSubnets = containerSubnets;
        this.vlans = vlans;
    }


    public UsedNetworkResources()
    {
    }


    public synchronized void addVni( long vni )
    {
        Preconditions.checkArgument( NumUtil.isLongBetween( vni, Common.MIN_VNI_ID, Common.MAX_VNI_ID ) );

        vnis.add( vni );
    }


    public synchronized void addVlan( int vlan )
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( vlan, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );

        vlans.add( vlan );
    }


    public synchronized void addP2pSubnet( String p2pSubnet )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pSubnet ) );

        p2pSubnets.add( IPUtil.getNetworkAddress( p2pSubnet ) );
    }


    public synchronized void addContainerSubnet( String containerSubnet )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerSubnet ) );

        containerSubnets.add( IPUtil.getNetworkAddress( containerSubnet ) );
    }


    public boolean p2pSubnetExists( String subnet )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subnet ) );

        String netAddress = IPUtil.getNetworkAddress( subnet );
        for ( String p2pSubnet : p2pSubnets )
        {
            if ( p2pSubnet.equalsIgnoreCase( netAddress ) )
            {
                return true;
            }
        }

        return false;
    }


    public boolean containerSubnetExists( String subnet )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subnet ) );

        String netAddress = IPUtil.getNetworkAddress( subnet );

        for ( String containerSubnet : containerSubnets )
        {
            if ( containerSubnet.equalsIgnoreCase( netAddress ) )
            {
                return true;
            }
        }

        return false;
    }


    public boolean vlanExists( int vLan )
    {
        return vlans.contains( vLan );
    }


    public boolean vniExists( long vni )
    {
        return vnis.contains( vni );
    }


    public Set<Long> getVnis()
    {
        return vnis;
    }


    public Set<String> getP2pSubnets()
    {
        return p2pSubnets;
    }


    public Set<String> getContainerSubnets()
    {
        return containerSubnets;
    }


    public Set<Integer> getVlans()
    {
        return vlans;
    }


    public synchronized int calculateFreeVlan()
    {

        for ( Integer nextVlan = Common.MIN_VLAN_ID; nextVlan <= Common.MAX_VLAN_ID; nextVlan++ )
        {
            //check in reserved vlans
            if ( vlans.contains( nextVlan ) )
            {
                continue;
            }

            //check in cached vlans
            if ( cachedVlans.getIfPresent( nextVlan ) != null )
            {
                continue;
            }

            //cache vlan to make it "look" reserved for parallel reservation attempts
            //when reserved vlans don't contain it yet
            cachedVlans.put( nextVlan, true );

            return nextVlan;
        }

        return -1;
    }
}
