package io.subutai.common.util;


import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.settings.Common;


public class IPUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger( IPUtil.class );


    private static long ipToLong( InetAddress ip )
    {
        byte[] octets = ip.getAddress();
        long result = 0;

        for ( byte octet : octets )
        {
            result <<= 8;
            result |= octet & 0xff;
        }
        return result;
    }


    public static boolean isValidIPRange( String ipStart, String ipEnd, String ipToCheck )
    {
        try
        {
            if ( "*".equals( ipStart ) || "".equals( ipStart ) )
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
        catch ( Exception e )
        {
            LOGGER.error( "Error parsing InetAddress", e );
            return false;
        }
    }


    public static boolean isValid( final String ip )
    {
        return !Strings.isNullOrEmpty( ip ) && ip.matches( Common.IP_REGEX );
    }


    public static String getLocalIpByInterfaceName( String interfaceName ) throws SocketException
    {
        Enumeration<InetAddress> addressEnumeration = NetworkInterface.getByName( interfaceName ).getInetAddresses();
        while ( addressEnumeration.hasMoreElements() )
        {
            InetAddress address = addressEnumeration.nextElement();
            if ( address instanceof Inet4Address )
            {
                return address.getHostAddress();
            }
        }
        return null;
    }


    public static Set<String> getLocalIps() throws SocketException
    {
        Set<String> localIps = Sets.newHashSet();
        Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();

        while ( netInterfaces.hasMoreElements() )
        {
            NetworkInterface networkInterface = netInterfaces.nextElement();
            Enumeration<InetAddress> addressEnumeration = networkInterface.getInetAddresses();
            while ( addressEnumeration.hasMoreElements() )
            {
                InetAddress address = addressEnumeration.nextElement();
                if ( address instanceof Inet4Address )
                {
                    localIps.add( address.getHostAddress() );
                }
            }
        }

        return localIps;
    }
}

