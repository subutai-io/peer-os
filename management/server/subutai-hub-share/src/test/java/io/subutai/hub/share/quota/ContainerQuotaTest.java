package io.subutai.hub.share.quota;


import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.subutai.hub.share.resource.ByteUnit;
import io.subutai.hub.share.resource.ContainerResourceType;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class ContainerQuotaTest
{
    @Test
    public void testContainerRamResource() throws IOException
    {
        String json = "{\"type\":\"ram\",\"value\":\"512MiB\"}";
        ObjectMapper mapper = new ObjectMapper();


        StringWriter sb = new StringWriter();
        ContainerRamResource r = new ContainerRamResource( "512MiB" );
        mapper.writeValue( sb, r );

        ContainerRamResource o = mapper.readValue( json, ContainerRamResource.class );

        assertEquals( o.getResource().getValue( ByteUnit.MB ).doubleValue(), 512.0 );
    }


    @Test
    public void testQuota() throws IOException
    {
        String json = "{\"resources\":{\"ROOTFS\":{\"resource\":{\"type\":\"rootfs\",\"value\":\"2.00000GiB\"},"
                + "\"threshold\":0},\"RAM\":{\"resource\":{\"type\":\"ram\",\"value\":\"512.000MiB\"},"
                + "\"threshold\":0},\"HOME\":{\"resource\":{\"type\":\"home\",\"value\":\"1024.00000KiB\"},"
                + "\"threshold\":0},\"CPU\":{\"resource\":{\"type\":\"cpu\",\"value\":\"25%\"},\"threshold\":0},"
                + "\"OPT\":{\"resource\":{\"type\":\"opt\",\"value\":\"5.00000GiB\"},\"threshold\":0},"
                + "\"VAR\":{\"resource\":{\"type\":\"var\",\"value\":\"5.00000GiB\"},\"threshold\":0}}}";
        ObjectMapper mapper = new ObjectMapper();

        ContainerQuota quota = mapper.readValue( json, ContainerQuota.class );

        assertEquals( 512.0,
                quota.get( ContainerResourceType.RAM ).getAsRamResource().getResource().getValue( ByteUnit.MB )
                     .doubleValue() );
        assertEquals( 25.0,
                quota.get( ContainerResourceType.CPU ).getAsCpuResource().getResource().getValue().doubleValue() );
        assertEquals( 1.0,
                quota.get( ContainerResourceType.HOME ).getAsDiskResource().getResource().getValue( ByteUnit.MB )
                     .doubleValue() );
    }


    @Test
    public void testQuota1() throws IOException
    {
        String json = "{\"containerSize\":\"SMALL\",\"resources\":{\"RAM\":{\"resource\":{\"type\":\"ram\","
                + "\"value\":\"512.000MiB\"},\"threshold\":0},\"HOME\":{\"resource\":{\"type\":\"home\","
                + "\"value\":\"1.00000GiB\"},\"threshold\":0},\"ROOTFS\":{\"resource\":{\"type\":\"rootfs\","
                + "\"value\":\"2.00000GiB\"},\"threshold\":0},\"OPT\":{\"resource\":{\"type\":\"opt\","
                + "\"value\":\"5.00000GiB\"},\"threshold\":0},\"CPU\":{\"resource\":{\"type\":\"cpu\","
                + "\"value\":\"25%\"},\"threshold\":0},\"NET\":{\"resource\":{\"type\":\"net\","
                + "\"value\":\"100.000000Kbps\"},\"threshold\":0},\"VAR\":{\"resource\":{\"type\":\"var\","
                + "\"value\":\"5.00000GiB\"},\"threshold\":0},\"CPUSET\":{\"resource\":{\"type\":\"cpuset\","
                + "\"value\":\"0-7\"},\"threshold\":0}}}";
        ObjectMapper mapper = new ObjectMapper();

        ContainerQuota quota = mapper.readValue( json, ContainerQuota.class );

        assertEquals( 512.0,
                quota.get( ContainerResourceType.RAM ).getAsRamResource().getResource().getValue( ByteUnit.MB )
                     .doubleValue() );
        assertEquals( 25.0,
                quota.get( ContainerResourceType.CPU ).getAsCpuResource().getResource().getValue().doubleValue() );
        assertEquals( 1.0,
                quota.get( ContainerResourceType.HOME ).getAsDiskResource().getResource().getValue( ByteUnit.GB )
                     .doubleValue() );
        assertEquals( "0-7", quota.get( ContainerResourceType.CPUSET ).getAsCpuSetResource().getResource().getValue() );
        assertEquals( 100.0D, quota.get( ContainerResourceType.NET ).getAsNetResource().getResource().getValue().doubleValue() );
    }
}
