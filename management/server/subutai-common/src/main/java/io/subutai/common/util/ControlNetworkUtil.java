package io.subutai.common.util;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Preconditions;

import io.subutai.common.protocol.ControlNetworkConfig;


/**
 * Control network utils.
 */
public class ControlNetworkUtil
{
    public static final long DEFAULT_TTL = 10000;
    public static String NETWORK_MASK = "255.255.255.0";
    public static String NETWORK_PREFIX = "10.200";
    public static final String IP_PATTERN = "^10\\.200\\..*";
    private final SubnetUtils.SubnetInfo info;
    private final String fingerprint;
    private final Set<String> usedAddresses = new HashSet<>();
    private final long secretKeyTtl;
    private final byte[] secretKey;
    private final String network;
    private final List<ControlNetworkConfig> configs;


    public ControlNetworkUtil( final String fingerprint, final String network, final byte[] secretKey,
                               final long secretKeyTtl, final List<ControlNetworkConfig> configs )
            throws ControlNetworkException
    {
        this.fingerprint = fingerprint;
        this.network = network;
        this.info = new SubnetUtils( network, NETWORK_MASK ).getInfo();
        this.secretKey = secretKey;
        this.secretKeyTtl = secretKeyTtl;
        this.configs = configs;
        for ( ControlNetworkConfig config : configs )
        {
            final String address = config.getAddress();
            if ( address == null )
            {
                throw new ControlNetworkException(
                        String.format( "Illegal control network config on peer '%s'. Address is null.",
                                config.getPeerId() ) );
            }
            if ( !info.isInRange( address ) )
            {
                throw new ControlNetworkException(
                        String.format( "Illegal control network config on peer '%s'. Address out of range.",
                                config.getPeerId() ) );
            }
            if ( usedAddresses.contains( address ) )
            {
                throw new ControlNetworkException(
                        String.format( "Illegal control network config on peer '%s'. Address already used.",
                                config.getPeerId() ) );
            }
            if ( !fingerprint.equals( config.getCommunityName() ) )
            {
                throw new ControlNetworkException(
                        String.format( "Illegal control network config on peer '%s'. Invalid fingerprint.",
                                config.getPeerId() ) );
            }
            usedAddresses.add( address );
        }
    }


    public boolean isClashes( final String fingerprint, ControlNetworkConfig config )
    {
        Preconditions.checkNotNull( config );
        Preconditions.checkNotNull( config.getUsedNetworks() );

        return !fingerprint.equals( config.getCommunityName() ) && config.getUsedNetworks().contains( this.network );
    }


    public void add( final ControlNetworkConfig config ) throws ControlNetworkException
    {
        if ( isClashes( fingerprint, config ) )
        {
            throw new ControlNetworkException( "Control networks clashes." );
        }
        String nextAddress = findNextAddress();
        usedAddresses.add( nextAddress );
        ControlNetworkConfig c =
                new ControlNetworkConfig( config.getPeerId(), nextAddress, fingerprint, secretKey, secretKeyTtl,
                        config.getUsedNetworks() );
        configs.add( c );
    }


    protected String findNextAddress() throws ControlNetworkException
    {
        for ( String address : info.getAllAddresses() )
        {
            if ( !usedAddresses.contains( address ) )
            {
                return address;
            }
        }

        throw new ControlNetworkException( "There are no free address on network: " + network );
    }


    public List<ControlNetworkConfig> getConfigs()
    {
        return configs;
    }


    public static List<ControlNetworkConfig> rebuild( final String fingerprint, final String network,
                                                      final byte[] secretKey, final long secretKeyTtl,
                                                      List<ControlNetworkConfig> configs )
    {
        List<ControlNetworkConfig> result = new ArrayList<>();
        final String[] addresses = new SubnetUtils( network, NETWORK_MASK ).getInfo().getAllAddresses();
        int counter = 0;
        for ( ControlNetworkConfig config : configs )
        {
            ControlNetworkConfig c =
                    new ControlNetworkConfig( config.getPeerId(), addresses[counter++], fingerprint, secretKey,
                            secretKeyTtl, null );
            result.add( c );
        }
        return result;
    }


    public static String findFreeNetwork( final List<ControlNetworkConfig> configs )
    {
        String result = null;
        Set<String> networks = new HashSet<>();
        for ( ControlNetworkConfig config : configs )
        {
            for ( String s : config.getUsedNetworks() )
            {
                networks.add( extractNetwork( s ) );
            }
        }

        int i = 0;

        while ( i < 255 && result == null )
        {
            String p = String.format( "%s.%d.0", NETWORK_PREFIX, i );
            if ( !networks.contains( p ) )
            {
                result = p;
            }
            i++;
        }

        return result;
    }


    public static String extractNetwork( final String ip )
    {
        if ( ip == null )
        {
            return null;
        }

        return new SubnetUtils( ip, NETWORK_MASK ).getInfo().getNetworkAddress();
    }
}
