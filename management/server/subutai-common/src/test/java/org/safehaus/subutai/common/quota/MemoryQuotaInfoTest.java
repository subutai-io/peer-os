package org.safehaus.subutai.common.quota;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


@RunWith( MockitoJUnitRunner.class )
public class MemoryQuotaInfoTest
{
    private MemoryQuotaInfo memoryQuotaInfo;


    @Before
    public void setUp() throws Exception
    {
        memoryQuotaInfo = new MemoryQuotaInfo( DiskQuotaUnit.BYTE, 5.5 );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( memoryQuotaInfo.getMemoryQuota() );
        assertNotNull( memoryQuotaInfo.getQuotaType() );
        assertNotNull( memoryQuotaInfo.getQuotaValue() );
        assertNotNull( memoryQuotaInfo.getQuotaKey() );
        assertNotNull( memoryQuotaInfo.getQuotaUnit() );
        assertNotNull( MemoryUnit.BYTES.getName() );
        assertNotNull( MemoryUnit.BYTES.getUnitIdx());
        assertNotNull( MemoryUnit.BYTES.getMemoryUnit( 0 ));
        assertNotNull( MemoryUnit.BYTES.getMemoryUnit( 1 ));
        assertNotNull( MemoryUnit.BYTES.getMemoryUnit( 2 ));
        assertNotNull( MemoryUnit.BYTES.getMemoryUnit( 3 ));
        assertNotNull( MemoryUnit.BYTES.getMemoryUnit( "b" ));
        assertNotNull( MemoryUnit.BYTES.getMemoryUnit( "kb" ));
        assertNotNull( MemoryUnit.BYTES.getMemoryUnit( "mb" ));
        assertNotNull( MemoryUnit.BYTES.getMemoryUnit( "gb" ));
        assertNull( MemoryUnit.BYTES.getMemoryUnit( 6 ) );
        assertNull( MemoryUnit.BYTES.getMemoryUnit( "test" ) );
    }
}