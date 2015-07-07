package io.subutai.common.security.utils.io;


import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.security.utils.io.HexUtil;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class HexUtilTest
{
    private HexUtil hexUtil;
    private byte[] bytes;

    @Before
    public void setUp() throws Exception
    {
        bytes = new byte[5];
        bytes[0] = 5;
        bytes[1] = 6;
        bytes[2] = 8;
    }


    @Test
    public void testHexStringToByteArray() throws Exception
    {
        assertNotNull( HexUtil.hexStringToByteArray( "5555" ) );
    }


    @Test
    public void testByteArrayToHexString() throws Exception
    {
        assertNotNull( HexUtil.byteArrayToHexString( bytes ) );
    }


    @Test
    public void testGetHexString() throws Exception
    {
        BigInteger bigInt = BigInteger.valueOf( 555 );
        assertNotNull( HexUtil.getHexString( bigInt ) );
    }


    @Test
    public void testGetHexString1() throws Exception
    {
        assertNotNull( HexUtil.getHexString( bytes ) );
    }


    @Test
    public void testGetHexClearDump() throws Exception
    {
        assertNotNull( HexUtil.getHexClearDump( bytes ) );
    }
}