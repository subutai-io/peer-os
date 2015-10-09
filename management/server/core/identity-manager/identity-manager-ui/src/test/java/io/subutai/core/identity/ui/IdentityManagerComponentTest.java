package io.subutai.core.identity.ui;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith( MockitoJUnitRunner.class )
public class IdentityManagerComponentTest
{
    private IdentityManagerComponent identityManagerComponent;

    @Mock
    IdentityManagerPortalModule identityManagerPortalModule;
    @Mock
    IdentityManager identityManager;

    @Before
    public void setUp() throws Exception
    {
        identityManagerComponent = new IdentityManagerComponent( identityManagerPortalModule, identityManager );
    }


    @Test
    public void testDispose() throws Exception
    {
        identityManagerComponent.dispose();
    }
}