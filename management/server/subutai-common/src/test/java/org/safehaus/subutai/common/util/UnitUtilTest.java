package org.safehaus.subutai.common.util;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class UnitUtilTest
{
    private UnitUtil unitUtil;


    @Before
    public void setUp() throws Exception
    {
        unitUtil = new UnitUtil();
    }


    @Test
    public void testConvert() throws Exception
    {
        assertNotNull( unitUtil.convert( 5.5, UnitUtil.Unit.B, UnitUtil.Unit.B ) );
        assertNotNull( unitUtil.convert( 5.5, UnitUtil.Unit.B, UnitUtil.Unit.KB ) );
        assertNotNull( unitUtil.convert( 5.5, UnitUtil.Unit.B, UnitUtil.Unit.MB ) );
        assertNotNull( unitUtil.convert( 5.5, UnitUtil.Unit.B, UnitUtil.Unit.GB ) );

        assertNotNull( unitUtil.convert( 5.5, UnitUtil.Unit.KB, UnitUtil.Unit.B ) );
        assertNotNull( unitUtil.convert( 5.5, UnitUtil.Unit.KB, UnitUtil.Unit.KB ) );
        assertNotNull( unitUtil.convert( 5.5, UnitUtil.Unit.KB, UnitUtil.Unit.MB ) );
        assertNotNull( unitUtil.convert( 5.5, UnitUtil.Unit.KB, UnitUtil.Unit.GB ) );

        assertNotNull( unitUtil.convert( 5.5, UnitUtil.Unit.MB, UnitUtil.Unit.B ) );
        assertNotNull( unitUtil.convert( 5.5, UnitUtil.Unit.MB, UnitUtil.Unit.KB ) );
        assertNotNull( unitUtil.convert( 5.5, UnitUtil.Unit.MB, UnitUtil.Unit.MB ) );
        assertNotNull( unitUtil.convert( 5.5, UnitUtil.Unit.MB, UnitUtil.Unit.GB ) );

        assertNotNull( unitUtil.convert( 5.5, UnitUtil.Unit.GB, UnitUtil.Unit.B ) );
        assertNotNull( unitUtil.convert( 5.5, UnitUtil.Unit.GB, UnitUtil.Unit.KB ) );
        assertNotNull( unitUtil.convert( 5.5, UnitUtil.Unit.GB, UnitUtil.Unit.MB ) );
        assertNotNull( unitUtil.convert( 5.5, UnitUtil.Unit.GB, UnitUtil.Unit.GB ) );
    }
}