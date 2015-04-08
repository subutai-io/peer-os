package org.safehaus.subutai.common.util;


import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class IPUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger( IPUtil.class );
    private static long ipToLong(InetAddress ip)
    {
        byte[] octets = ip.getAddress();
        long result = 0;

        for (byte octet : octets)
        {
            result <<= 8;
            result |= octet & 0xff;
        }
        return result;
    }

    public static boolean isValidIPRange(String ipStart, String ipEnd, String ipToCheck)
    {
        try
        {
            if("*".equals( ipStart ) || "".equals( ipStart ))
            {
                return true;
            }
            else
            {
                long ipLo = ipToLong( InetAddress.getByName( ipStart ) );
                long ipHi = ipToLong( InetAddress.getByName( ipEnd ) );
                long ipToTest = ipToLong( InetAddress.getByName( ipToCheck ) );
                return ( ipToTest >= ipLo && ipToTest <= ipHi );
            }
        }
        catch (Exception e)
        {
            LOGGER.error( "Error parsing InetAddress", e );
            return false;
        }
    }
}

