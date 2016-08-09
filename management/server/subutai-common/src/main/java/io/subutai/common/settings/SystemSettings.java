package io.subutai.common.settings;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.util.NumUtil;
import io.subutai.common.util.ServiceLocator;


public class SystemSettings
{
    private static final Logger LOG = LoggerFactory.getLogger( SystemSettings.class );

    public static final int DEFAULT_P2P_PORT_START_RANGE = 0;
    public static final int DEFAULT_P2P_PORT_END_RANGE = 65535;

    private static PropertiesConfiguration PROPERTIES = null;

    static
    {
        loadProperties();
    }

    public static void loadProperties()
    {
        try
        {
            PROPERTIES = new PropertiesConfiguration( String.format( "%s/subutaisystem.cfg", Common.KARAF_ETC ) );
        }
        catch ( ConfigurationException e )
        {
            throw new RuntimeException( "Failed to load subutaisettings.cfg file.", e );
        }
    }


    private static LocalPeer getLocalPeer()
    {
        return ServiceLocator.getServiceNoCache( LocalPeer.class );
    }


    public static String getPublicUrl()
    {
        LocalPeer localPeer = getLocalPeer();
        if ( localPeer != null && localPeer.isInitialized() )
        {
            return getLocalPeer().getPeerInfo().getPublicUrl();
        }
        else
        {
            return Common.DEFAULT_PUBLIC_URL;
        }
    }


    public static int getPublicSecurePort()
    {
        LocalPeer localPeer = getLocalPeer();
        if ( localPeer != null && localPeer.isInitialized() )
        {
            return getLocalPeer().getPeerInfo().getPublicSecurePort();
        }
        else
        {
            return Common.DEFAULT_PUBLIC_SECURE_PORT;
        }
    }


    protected static void saveProperty( final String name, final Object value )
    {
        try
        {
            PROPERTIES.setProperty( name, value );
            PROPERTIES.save();
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in saving subutaisettings.cfg file.", e );
        }
    }


    public static int getP2pPortStartRange()
    {
        return PROPERTIES.getInt( "p2pPortStartRange", DEFAULT_P2P_PORT_START_RANGE );
    }


    public static int getP2pPortEndRange()
    {
        return PROPERTIES.getInt( "p2pPortEndRange", DEFAULT_P2P_PORT_END_RANGE );
    }


    public static void setP2pPortRange( final int p2pPortStartRange, final int p2pPortEndRange )
    {
        Preconditions.checkArgument(
                NumUtil.isIntBetween( p2pPortStartRange, DEFAULT_P2P_PORT_START_RANGE, DEFAULT_P2P_PORT_END_RANGE ) );
        Preconditions.checkArgument(
                NumUtil.isIntBetween( p2pPortStartRange, DEFAULT_P2P_PORT_START_RANGE, DEFAULT_P2P_PORT_END_RANGE ) );
        Preconditions.checkArgument( p2pPortEndRange > p2pPortStartRange );

        saveProperty( "p2pPortStartRange", p2pPortStartRange );
        saveProperty( "p2pPortEndRange", p2pPortEndRange );
    }
}

