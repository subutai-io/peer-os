package io.subutai.common.settings;


import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.exception.ActionFailedException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.util.NumUtil;
import io.subutai.common.util.ServiceLocator;


public class SystemSettings
{
    private static final Logger LOG = LoggerFactory.getLogger( SystemSettings.class );

    private static final int DEFAULT_P2P_PORT_START_RANGE = Common.MIN_PORT;
    private static final int DEFAULT_P2P_PORT_END_RANGE = Common.MAX_PORT;
    private static final String DEFAULT_HUB_IP = "hub.subut.ai";
    private static final String HUB_IP_KEY = "hubIp";
    private static final String P2P_PORT_START_RANGE_KEY = "p2pPortStartRange";
    private static final String P2P_PORT_END_RANGE_KEY = "p2pPortEndRange";

    private PropertiesConfiguration PROPERTIES = null;
    private long lastSettingsReloadTs = 0L;


    public SystemSettings()
    {
        loadSettings();
    }


    private void loadSettings()
    {
        try
        {
            PROPERTIES = new PropertiesConfiguration( String.format( "%s/subutaisystem.cfg", Common.KARAF_ETC ) );
        }
        catch ( ConfigurationException e )
        {
            throw new ActionFailedException( "Failed to load subutaisettings.cfg file.", e );
        }
    }


    private void checkSettingsFreshness()
    {
        if ( System.currentTimeMillis() - lastSettingsReloadTs >= TimeUnit.SECONDS.toMillis( 30 ) )
        {
            lastSettingsReloadTs = System.currentTimeMillis();

            loadSettings();
        }
    }


    private LocalPeer getLocalPeer()
    {
        return ServiceLocator.getServiceOrNull( LocalPeer.class );
    }


    public String getPublicUrl()
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


    public int getPublicSecurePort()
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


    protected void saveProperty( final String name, final Object value )
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


    public int getP2pPortStartRange()
    {
        checkSettingsFreshness();

        return PROPERTIES.getInt( P2P_PORT_START_RANGE_KEY, DEFAULT_P2P_PORT_START_RANGE );
    }


    public int getP2pPortEndRange()
    {
        checkSettingsFreshness();

        return PROPERTIES.getInt( P2P_PORT_END_RANGE_KEY, DEFAULT_P2P_PORT_END_RANGE );
    }


    public void setP2pPortRange( final int p2pPortStartRange, final int p2pPortEndRange )
    {
        Preconditions.checkArgument(
                NumUtil.isIntBetween( p2pPortStartRange, DEFAULT_P2P_PORT_START_RANGE, DEFAULT_P2P_PORT_END_RANGE ) );
        Preconditions.checkArgument(
                NumUtil.isIntBetween( p2pPortStartRange, DEFAULT_P2P_PORT_START_RANGE, DEFAULT_P2P_PORT_END_RANGE ) );
        Preconditions.checkArgument( p2pPortEndRange > p2pPortStartRange );

        saveProperty( P2P_PORT_START_RANGE_KEY, p2pPortStartRange );
        saveProperty( P2P_PORT_END_RANGE_KEY, p2pPortEndRange );
    }


    public String getHubIp()
    {
        checkSettingsFreshness();

        return PROPERTIES.getString( HUB_IP_KEY, DEFAULT_HUB_IP );
    }


    public void setHubIp( String hubIp )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hubIp ) );

        saveProperty( HUB_IP_KEY, hubIp );
    }
}

