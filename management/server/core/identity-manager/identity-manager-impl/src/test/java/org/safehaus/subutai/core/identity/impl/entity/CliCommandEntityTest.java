package org.safehaus.subutai.core.identity.impl.entity;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith( MockitoJUnitRunner.class )
public class CliCommandEntityTest
{
    private CliCommandEntity cliCommandEntity;

    @Mock
    Object object;


    @Before
    public void setUp() throws Exception
    {
        cliCommandEntity = new CliCommandEntity();
        cliCommandEntity = new CliCommandEntity( "scope", "name" );
    }


    @Test
    public void testGetScope() throws Exception
    {
        assertNotNull( cliCommandEntity.getScope() );
    }


    @Test
    public void testGetName() throws Exception
    {
        assertNotNull( cliCommandEntity.getName() );
    }


    @Test
    public void testGetCommand() throws Exception
    {
        assertNotNull( cliCommandEntity.getCommand() );
    }


    @Test
    public void testToString() throws Exception
    {
        assertNotNull( cliCommandEntity.toString() );
    }


    @Test
    public void testEquals() throws Exception
    {
        assertFalse( cliCommandEntity.equals( object ) );
        assertTrue( cliCommandEntity.equals( cliCommandEntity ) );
    }


    @Test
    public void testHashCode() throws Exception
    {
        cliCommandEntity.hashCode();
    }
}