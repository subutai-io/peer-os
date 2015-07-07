package io.subutai.core.identity.impl.entity;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.core.identity.impl.entity.CliCommandPK;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith( MockitoJUnitRunner.class )
public class CliCommandPKTest
{
    private CliCommandPK cliCommandPK;

    @Mock
    Object object;

    @Before
    public void setUp() throws Exception
    {
        cliCommandPK = new CliCommandPK(  );
        cliCommandPK = new CliCommandPK( "scope", "name" );
    }


    @Test
    public void testGetScope() throws Exception
    {
        assertNotNull(cliCommandPK.getScope());
    }


    @Test
    public void testGetName() throws Exception
    {
        assertNotNull( cliCommandPK.getName() );
    }


    @Test
    public void testEquals() throws Exception
    {
        assertFalse( cliCommandPK.equals( object ) );
        assertTrue( cliCommandPK.equals( cliCommandPK ) );
    }


    @Test
    public void testHashCode() throws Exception
    {
        cliCommandPK.hashCode();
    }
}