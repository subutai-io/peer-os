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
public class PortalModuleScopeEntityTest
{
    private PortalModuleScopeEntity moduleScopeEntity;

    @Mock
    Object object;


    @Before
    public void setUp() throws Exception
    {
        moduleScopeEntity = new PortalModuleScopeEntity( "moduleKey", "moduleName" );
    }


    @Test
    public void testGetModuleKey() throws Exception
    {
        assertNotNull( moduleScopeEntity.getModuleKey() );
    }


    @Test
    public void testGetModuleName() throws Exception
    {
        assertNotNull( moduleScopeEntity.getModuleName() );
    }


    @Test
    public void testEquals() throws Exception
    {
        assertFalse( moduleScopeEntity.equals( object ) );
        assertTrue( moduleScopeEntity.equals( moduleScopeEntity ) );
    }


    @Test
    public void testHashCode() throws Exception
    {
        moduleScopeEntity.hashCode();
    }


    @Test
    public void testToString() throws Exception
    {
        moduleScopeEntity.toString();
    }
}