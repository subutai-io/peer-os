package io.subutai.common.quota;


import org.junit.Before;
import org.junit.Test;

import io.subutai.common.quota.Memory;
import io.subutai.common.quota.MemoryUnit;

import static org.junit.Assert.assertNotNull;


public class MemoryTest
{
    private Memory memory;
    private Memory memory2;
    private Memory memory3;


    @Before
    public void setUp() throws Exception
    {
        memory = new Memory( "test" );
        memory2 = new Memory( MemoryUnit.BYTES, ( long ) 1000 );
        memory3 = new Memory( MemoryUnit.EB, ( long ) 1000 );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( memory2.getUnit() );
        assertNotNull( memory2.getValue() );
        assertNotNull( memory2.toString() );
        assertNotNull( memory3.toString() );
    }
}