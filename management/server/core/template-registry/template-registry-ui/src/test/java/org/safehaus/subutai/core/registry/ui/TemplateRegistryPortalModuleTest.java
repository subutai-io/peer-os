package org.safehaus.subutai.core.registry.ui;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith( MockitoJUnitRunner.class )
public class TemplateRegistryPortalModuleTest
{
    private TemplateRegistryPortalModule templateRegistryPortalModule;
    @Mock
    TemplateRegistry templateRegistry;

    @Before
    public void setUp() throws Exception
    {
        templateRegistryPortalModule = new TemplateRegistryPortalModule();
        templateRegistryPortalModule.setRegistryManager( templateRegistry );
    }


    @Test
    public void testProperties()
    {
        assertEquals( TemplateRegistryPortalModule.MODULE_NAME, templateRegistryPortalModule.getId() );
        assertEquals( TemplateRegistryPortalModule.MODULE_NAME, templateRegistryPortalModule.getName() );
        assertNotNull( templateRegistryPortalModule.getImage() );
        assertTrue( templateRegistryPortalModule.isCorePlugin() );
    }


    @Test
    public void testCreateComponent() throws Exception
    {
        assertTrue( templateRegistryPortalModule.createComponent() instanceof TemplateRegistryComponent );
    }
}