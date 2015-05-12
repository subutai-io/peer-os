package org.safehaus.subutai.common.util;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith( MockitoJUnitRunner.class )
public class UUIDUtilTest
{
    private UUIDUtil uuidUtil;


    @Before
    public void setUp() throws Exception
    {
        uuidUtil = new UUIDUtil();
    }


    @Test
    public void testProperties() throws Exception
    {
        byte[] bytes = new byte[2];
        bytes[0] = 5;
        bytes[1] = 6;
        assertNotNull( UUIDUtil.generateRandomUUID() );
        assertNotNull( UUIDUtil.generateTimeBasedUUID() );
        assertNotNull( UUIDUtil.generateUUIDFromBytes( bytes ) );
        assertNotNull( UUIDUtil.generateUUIDFromString( UUIDUtil.generateRandomUUID().toString() ));
        assertTrue( UUIDUtil.isStringAUuid( UUIDUtil.generateRandomUUID().toString() ) );
        assertFalse( UUIDUtil.isStringAUuid( "test" ) );
    }
}