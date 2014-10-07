package org.safehaus.subutai.core.template.impl;


import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.mock;


/**
 * Created by timur on 10/6/14.
 */
public class TemplateManagerImplTest
{

    TemplateManagerImpl templateManager;


    @Before
    public void setUpMethod()
    {
        templateManager = new TemplateManagerImpl();
        templateManager.setAgentManager( new AgentManagerFake() );
    }


    @After
    public void tearDown()
    {
        templateManager.destroy();
    }


    public void testGetMasterTemplateName()
    {
        String result = templateManager.getMasterTemplateName();
        assertNotNull( result );
        assertEquals( MockUtils.MASTER_TEMPLATE_NAME, result );
    }


    @Test
    public void testCloneOne()
    {

        UUID envId = UUID.randomUUID();
        CommandRunner commandRunner = mock( CommandRunner.class );
        templateManager.setCommandRunner( MockUtils.getHardCodedCloneCommandRunner( true, true, 0, "", "" ) );

        TemplateRegistry templateRegistry = MockUtils.getHardCodedTemplateRegistry();
        templateManager.setTemplateRegistry( templateRegistry );

        templateManager.init();

        boolean result = templateManager
                .clone( MockUtils.PHYSICAL_HOSTNAME, MockUtils.MASTER_TEMPLATE_NAME, MockUtils.LXC_HOSTNAME,
                        envId.toString() );
        assertTrue( result );
    }
}
