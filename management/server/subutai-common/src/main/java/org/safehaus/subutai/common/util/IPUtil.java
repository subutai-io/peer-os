package org.safehaus.subutai.common.util;


import java.net.InetAddress;


/**
 * Created by nisakov on 3/6/15.
 */
public class IPUtil
{
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
            e.printStackTrace();
            return false;
        }
    }
}

