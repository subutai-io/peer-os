package io.subutai.bazaar.share;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.bazaar.share.parser.CpuSetResourceValueParser;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class CpuSetResourceValueParserTest
{
    private CpuSetResourceValueParser cpuSetResourceValueParser = CpuSetResourceValueParser.getInstance();


    @Test
    public void testValidValues()
    {
        assertEquals( "0-2", cpuSetResourceValueParser.parse( "0-2" ).getValue() );
        assertEquals( "0", cpuSetResourceValueParser.parse( "0" ).getValue() );
    }


    @Test( expected = IllegalArgumentException.class )
    public void testInvalidValue1()
    {
        cpuSetResourceValueParser.parse( "0-a" ).getValue();
    }


    @Test( expected = IllegalArgumentException.class )
    public void testInvalidValue2()
    {
        cpuSetResourceValueParser.parse( "0-" ).getValue();
    }


    @Test( expected = IllegalArgumentException.class )
    public void testInvalidValue3()
    {
        cpuSetResourceValueParser.parse( "10" ).getValue();
    }
}
