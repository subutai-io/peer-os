package io.subutai.common.util;


import java.util.Set;


/**
 * P2P utils.
 */
public abstract class P2PUtil
{
    public static String P2P_SUBNET_MASK = "255.255.255.0";
    public static final String P2P_INTERFACE_IP_PATTERN = "^10\\..*";


    public static String findFreeSubnet( final Set<String> excludedNetworks )
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


    public static String generateHash( final String envId )
    {
        return String.format( "swarm-%s", envId );
    }


    public static String generateInterfaceName( final int vlan )
    {
        return String.format( "p2p-%d", vlan );
    }
}
