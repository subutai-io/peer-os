package io.subutai.common.util;


import java.util.Random;
import java.util.Set;

import com.google.common.base.Preconditions;

import io.subutai.common.protocol.Tunnels;
import io.subutai.common.settings.Common;


/**
 * P2P utils.
 */
public abstract class P2PUtil
{

    private P2PUtil()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    public static final String P2P_SUBNET_MASK = "255.255.255.0";


    public static String generateHash( final String envId )
    {
        return String.format( "swarm-%s", envId );
    }


    public static String generateInterfaceName( final int vlan )
    {
        return String.format( "%s%d", Common.P2P_INTERFACE_PREFIX, vlan );
    }


    public static int getVlanFromInterfaceName( final String p2pInterfaceName )
    {
        return Integer.parseInt( p2pInterfaceName.replace( Common.P2P_INTERFACE_PREFIX, "" ) );
    }


    public static String generateContainerSubnet( final Set<String> excludedIPs )
    {
        int maxIterations = 4080;
        int currentIteration = 0;
        String ip;

        do
        {
            ip = String.format( "172.%d.%d.0", generateIntInRange( 16, 31 ), generateIntInRange( 0, 254 ) );
            currentIteration++;
        }
        while ( excludedIPs.contains( ip ) && currentIteration < maxIterations );

        return ip;
    }


    public static String generateP2PSubnet( final Set<String> excludedIPs )
    {
        int maxIterations = 62220;
        int currentIteration = 0;
        String ip;

        do
        {
            ip = String.format( "10.%d.%d.0", generateIntInRange( 11, 254 ), generateIntInRange( 0, 254 ) );
            currentIteration++;
        }
        while ( excludedIPs.contains( ip ) && currentIteration < maxIterations );

        return ip;
    }


    public static String generateTunnelName( Tunnels tunnels )
    {
        int maxIterations = 90000;
        int currentIteration = 0;
        String name;


        do
        {
            int n = generateIntInRange( 10000, 99999 );
            name = String.format( "tunnel-%d", n );
            currentIteration++;
        }
        while ( tunnels.findByName( name ) != null && currentIteration < maxIterations );

        if ( tunnels.findByName( name ) != null )
        {
            return null;
        }

        return name;
    }


    private static int generateIntInRange( int from, int to )
    {
        Preconditions.checkArgument( from < to );
        Preconditions.checkArgument( to < Integer.MAX_VALUE );

        Random rnd = new Random();

        return from + rnd.nextInt( to + 1 - from );
    }
}
