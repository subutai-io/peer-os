package io.subutai.common.network;


import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import io.subutai.common.settings.Common;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.IPUtil;
import io.subutai.common.util.NumUtil;


public class UsedNetworkResources
{
    @JsonProperty( "vnis" )
    Set<Long> vnis = Sets.newConcurrentHashSet();
    @JsonProperty( "p2pSubnets" )
    Set<String> p2pSubnets = Sets.newConcurrentHashSet();
    @JsonProperty( "containerSubnets" )
    Set<String> containerSubnets = Sets.newConcurrentHashSet();
    @JsonProperty( "vlans" )
    Set<Integer> vlans = Sets.newConcurrentHashSet();


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


    //TODO review this method when "p2p show" returns p2p names
    //it should not just increase vlan but should reuse empty slots with lowest vlan first
    public int calculateFreeVlan()
    {
        //workaround implementation
        Path vlanFile = Paths.get( Common.KARAF_DATA, "current_vlan.dat" );
        File file = vlanFile.toFile();
        try
        {
            int vlan = file.createNewFile() ? Common.MIN_VLAN_ID - 1 :
                       Integer.parseInt( Files.readFirstLine( file, Charset.defaultCharset() ) );

            if ( vlan < Common.MAX_VLAN_ID )
            {
                vlan++;
            }
            else
            {
                vlan = Common.MIN_VLAN_ID;
            }

            Files.write( String.valueOf( vlan ).getBytes(), file );

            return vlan;
        }
        //default implementation
        catch ( Exception e )
        {
            if ( vlans.isEmpty() )
            {
                return Common.MIN_VLAN_ID;
            }

            List<Integer> sortedVlans = CollectionUtil.asSortedList( vlans );

            int maxUsedVlan = sortedVlans.get( sortedVlans.size() - 1 );

            if ( maxUsedVlan + 1 <= Common.MAX_VLAN_ID )
            {
                return maxUsedVlan + 1;
            }

            return -1;
        }
    }
}
