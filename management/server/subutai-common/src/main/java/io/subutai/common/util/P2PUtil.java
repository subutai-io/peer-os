package io.subutai.common.util;


import java.util.Set;

import com.google.common.base.Preconditions;


/**
 * P2P utils.
 */
public abstract class P2PUtil
{
    public static String P2P_SUBNET_MASK = "255.255.255.0";
    public static final String P2P_INTERFACE_IP_PATTERN = "^10\\..*";


    public static String findFreeTunnelNetwork( final Set<String> excludedNetworks )
    {
        String result = null;
        int i = 11;
        int j = 0;

        while ( result == null && i < 200 )
        {
            String s = String.format( "10.%d.%d.0", i, j );
            if ( !excludedNetworks.contains( s ) )
            {
                result = s;
            }

            j++;
            if ( j > 254 )
            {
                i++;
                j = 0;
            }
        }

        return result;
    }


    public static String findFreeAddress( String network, Set<String> excludedAddresses )
    {
        Preconditions.checkNotNull( network, "Network address could not be null." );
        Preconditions.checkNotNull( excludedAddresses, "Excluded address set could not be null." );

        String fmt = network.replaceAll( ".\\d$", ".%s" );

        String result = null;
        for ( int i = 1; i < 255; i++ )
        {
            String nextAddress = String.format( fmt, i );
            if ( !excludedAddresses.contains( nextAddress ) )
            {
                result = nextAddress;
                break;
            }
        }

        return result;
    }


    public static String generateCommunityName( final String ip )
    {
        return String.format( "com_%s", ip.replace( ".", "_" ) );
    }


    public static String generateInterfaceName( final String ip )
    {
        return String.format( "p2p_%s", ip.replace( ".", "_" ) );
    }
}
