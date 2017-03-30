package io.subutai.hub.share.quota;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.subutai.hub.share.parser.CommonResourceValueParser;
import io.subutai.hub.share.resource.ContainerResourceType;
import io.subutai.hub.share.resource.ResourceValueParser;


/**
 * Container size enumeration
 */
public enum ContainerSize
{

    TINY, SMALL, MEDIUM, LARGE, HUGE, CUSTOM;

    private static Logger LOGGER = LoggerFactory.getLogger( ContainerSize.class );

    private static Properties quotaSettings = new Properties();

    private static EnumMap<ContainerSize, ContainerQuota> containerQuotas = new EnumMap<>( ContainerSize.class );

    static
    {
        try
        {
            InputStream is = new FileInputStream( System.getProperty( "karaf.etc" ) + "/quota.cfg" );

            quotaSettings.load( is );

            initQuotas();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error loading quota.cfg: {}", e.getMessage() );

        }
    }

    @JsonIgnore
    private static void initQuotas() throws QuotaException
    {
        for ( ContainerSize containerSize : ContainerSize.values() )
        {
            if ( containerSize == ContainerSize.CUSTOM )
            {
                continue;
            }

            try
            {
                final ContainerQuota quota = new ContainerQuota( containerSize );

                ResourceValueParser quotaParser = getResourceValueParser( ContainerResourceType.RAM );
                quota.add( new Quota( ContainerResourceFactory.createContainerResource( ContainerResourceType.RAM,
                        quotaParser.parse( quotaSettings
                                .getProperty( containerSize.name() + "." + ContainerResourceType.RAM.getKey() ) ) ),
                        0 ) );

                quotaParser = getResourceValueParser( ContainerResourceType.CPU );
                quota.add( new Quota( ContainerResourceFactory.createContainerResource( ContainerResourceType.CPU,
                        quotaParser.parse( quotaSettings
                                .getProperty( containerSize.name() + "." + ContainerResourceType.CPU.getKey() ) ) ),
                        0 ) );

                quotaParser = getResourceValueParser( ContainerResourceType.DISK );
                quota.add( new Quota( ContainerResourceFactory.createContainerResource( ContainerResourceType.DISK,
                        quotaParser.parse( quotaSettings.getProperty(
                                containerSize.name() + "." + ContainerResourceType.DISK.getKey() ) ) ), 0 ) );

                containerQuotas.put( containerSize, quota );

                LOGGER.debug( quota.toString() );
            }
            catch ( Exception e )
            {
                throw new QuotaException( String.format( "Could not parse quota settings for %s.", containerSize ) );
            }
        }
    }


    @JsonIgnore
    private static ResourceValueParser getResourceValueParser( final ContainerResourceType containerResourceType )
            throws QuotaException
    {
        return CommonResourceValueParser.getInstance( containerResourceType );
    }


    @JsonIgnore
    public static Set getContainerSizesDescription() throws IOException
    {
        return Collections.unmodifiableSet( quotaSettings.entrySet() );
    }


    @JsonIgnore
    public static ContainerQuota getDefaultContainerQuota( final ContainerSize containerSize )
    {
        return containerQuotas.get( containerSize );
    }


    @JsonIgnore
    public static Map<ContainerSize, ContainerQuota> getDefaultQuotas()
    {
        return Collections.unmodifiableMap( containerQuotas );
    }
}
