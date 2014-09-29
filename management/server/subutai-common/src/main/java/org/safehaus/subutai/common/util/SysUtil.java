package org.safehaus.subutai.common.util;


import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author dilshat
 */
public class SysUtil
{

    public static String getMacAddress()
    {
        try
        {
            //            String mac = null;
            InetAddress ip = InetAddress.getLocalHost();

            Enumeration e = NetworkInterface.getNetworkInterfaces();

            while ( e.hasMoreElements() )
            {

                NetworkInterface n = ( NetworkInterface ) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while ( ee.hasMoreElements() )
                {
                    InetAddress i = ( InetAddress ) ee.nextElement();
                    if ( !i.isLoopbackAddress() && !i.isLinkLocalAddress() && i.isSiteLocalAddress() )
                    {
                        ip = i;
                    }
                }
            }

            NetworkInterface network = NetworkInterface.getByInetAddress( ip );
            byte[] mac_byte = network.getHardwareAddress();

            StringBuilder sb = new StringBuilder();
            for ( int i = 0; i < mac_byte.length; i++ )
            {
                sb.append( String.format( "%02X%s", mac_byte[i], ( i < mac_byte.length - 1 ) ? "-" : "" ) );
            }
            return sb.toString();
        }
        catch ( Exception ex )
        {
            Logger.getLogger( SysUtil.class.getName() ).log( Level.SEVERE, null, ex );
        }
        return null;
    }


    public static String getMacAddress2()
    {
        try
        {

            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            while ( networks.hasMoreElements() )
            {
                NetworkInterface network = networks.nextElement();
                byte[] mac = network.getHardwareAddress();

                if ( mac != null )
                {

                    StringBuilder sb = new StringBuilder();
                    for ( int i = 0; i < mac.length; i++ )
                    {
                        sb.append( String.format( "%02X%s", mac[i], ( i < mac.length - 1 ) ? "-" : "" ) );
                    }
                    return sb.toString();
                }
            }
        }
        catch ( Exception ex )
        {
            Logger.getLogger( SysUtil.class.getName() ).log( Level.SEVERE, null, ex );
        }
        return null;
    }


    public static String getHostName()
    {
        try
        {
            return InetAddress.getLocalHost().getHostName();
        }
        catch ( UnknownHostException ex )
        {
            Logger.getLogger( SysUtil.class.getName() ).log( Level.SEVERE, null, ex );
        }
        return null;
    }


    public static List<String> getIps()
    {

        List<String> ips = new ArrayList<String>();
        try
        {
            Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
            for (; n.hasMoreElements(); )
            {
                NetworkInterface e = n.nextElement();
                Enumeration<InetAddress> a = e.getInetAddresses();
                for (; a.hasMoreElements(); )
                {
                    InetAddress addr = a.nextElement();
                    ips.add( addr.getHostAddress() );
                }
            }
        }
        catch ( SocketException ex )
        {
            Logger.getLogger( SysUtil.class.getName() ).log( Level.SEVERE, null, ex );
        }

        return ips;
    }


    public static String getJarLocation()
    {
        String fullPath = getJarFullPath();
        String jarName = new File( fullPath ).getName();
        return fullPath.replace( "/" + jarName, "" );
    }


    public static String getJarFullPath()
    {
        return SysUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    }
}
