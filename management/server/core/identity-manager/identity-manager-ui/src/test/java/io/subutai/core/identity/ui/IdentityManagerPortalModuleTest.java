package io.subutai.core.identity.ui;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith( MockitoJUnitRunner.class )
public class IdentityManagerPortalModuleTest
{
    private IdentityManagerPortalModule identityManagerPortalModule;

    @Mock
    IdentityManager identityManager;


    @Before
    public void setUp() throws Exception
    {
        identityManagerPortalModule = new IdentityManagerPortalModule();
        identityManagerPortalModule.setIdentityManager( identityManager );
    }


    @Test
    public void testSetIdentityManager() throws Exception
    {
        identityManagerPortalModule.setIdentityManager( identityManager );
    }


    @Test
    public void testInit() throws Exception
    {
        identityManagerPortalModule.init();
    }


    @Test
    public void testDestroy() throws Exception
    {
        identityManagerPortalModule.destroy();
    }


    @Test
    public void testGetId() throws Exception
    {
        assertEquals("Identity Manager",identityManagerPortalModule.getId());
    }


    @Test
    public void testGetName() throws Exception
    {
        assertEquals( "Identity Manager", identityManagerPortalModule.getName() );
    }


    @Test
    public void testGetImage() throws Exception
    {
        identityManagerPortalModule.getImage();
    }


    @Test
    public void testCreateComponent() throws Exception
    {
        identityManagerPortalModule.createComponent();
    }


    @Test
    public void testIsCorePlugin() throws Exception
    {
        assertTrue( identityManagerPortalModule.isCorePlugin() );
    }
}