package io.subutai.core.lxc.quota.impl;


import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.resource.ByteUnit;
import io.subutai.common.resource.ByteValueResource;
import io.subutai.common.resource.DiskResource;
import io.subutai.common.resource.ResourceType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class DiskQuotaTest
{
    private DiskResource diskQuota;


    @Before
    public void setUp() throws Exception
    {
        diskQuota = new DiskResource( new BigDecimal( "1024" ), 0.0, "model", 123, 123, true );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( diskQuota.getResourceType() );
        assertEquals( ResourceType.DISK, diskQuota.getResourceType() );
        assertEquals( 1024, diskQuota.getResourceValue().getValue().intValue() );
        assertEquals( 1, diskQuota.getResourceValue().getValue( ByteUnit.KB ).intValue() );
        assertNotNull( diskQuota.getPrintValue() );
        assertNotNull( diskQuota.getWriteValue() );
    }
}