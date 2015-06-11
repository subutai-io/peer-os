package org.safehaus.subutai.common.util;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class UnitUtilTest
{
    private static final double DELTA = 1e-15;

    @Test
    public void testConvert() throws Exception
    {
        assertNotNull( UnitUtil.convert( 5.5, UnitUtil.Unit.B, UnitUtil.Unit.B ) );
        assertNotNull( UnitUtil.convert( 5.5, UnitUtil.Unit.B, UnitUtil.Unit.KB ) );
        assertNotNull( UnitUtil.convert( 5.5, UnitUtil.Unit.B, UnitUtil.Unit.MB ) );
        assertNotNull( UnitUtil.convert( 5.5, UnitUtil.Unit.B, UnitUtil.Unit.GB ) );

        assertNotNull( UnitUtil.convert( 5.5, UnitUtil.Unit.KB, UnitUtil.Unit.B ) );
        assertNotNull( UnitUtil.convert( 5.5, UnitUtil.Unit.KB, UnitUtil.Unit.KB ) );
        assertNotNull( UnitUtil.convert( 5.5, UnitUtil.Unit.KB, UnitUtil.Unit.MB ) );
        assertNotNull( UnitUtil.convert( 5.5, UnitUtil.Unit.KB, UnitUtil.Unit.GB ) );

        assertNotNull( UnitUtil.convert( 5.5, UnitUtil.Unit.MB, UnitUtil.Unit.B ) );
        assertNotNull( UnitUtil.convert( 5.5, UnitUtil.Unit.MB, UnitUtil.Unit.KB ) );
        assertNotNull( UnitUtil.convert( 5.5, UnitUtil.Unit.MB, UnitUtil.Unit.MB ) );
        assertNotNull( UnitUtil.convert( 5.5, UnitUtil.Unit.MB, UnitUtil.Unit.GB ) );

        assertNotNull( UnitUtil.convert( 5.5, UnitUtil.Unit.GB, UnitUtil.Unit.B ) );
        assertNotNull( UnitUtil.convert( 5.5, UnitUtil.Unit.GB, UnitUtil.Unit.KB ) );
        assertNotNull( UnitUtil.convert( 5.5, UnitUtil.Unit.GB, UnitUtil.Unit.MB ) );
        assertNotNull( UnitUtil.convert( 5.5, UnitUtil.Unit.GB, UnitUtil.Unit.GB ) );
    }


    @Test
    public void testGetBytesInMb()
    {
        double result = UnitUtil.getBytesInMb( 1024 * 1024 );
        assertEquals( 1.0, result, DELTA );
        result = UnitUtil.getBytesInMb( 1024 * 1024 * 4 );
        assertEquals( 4.0, result, DELTA );
    }
}