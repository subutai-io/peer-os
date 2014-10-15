package org.safehaus.subutai.core.template.impl;


import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.template.api.ActionType;
import org.safehaus.subutai.core.template.api.TemplateException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


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


//    @Test
    public void testCloneParentTemplateExists() throws TemplateException
    {

        UUID envId = UUID.randomUUID();
        //        CommandRunner commandRunner = mock( CommandRunner.class );
        //        templateManager.setCommandRunner( MockUtils.getHardCodedCloneCommandRunner( true, true, 0, "", "" ) );

        TemplateRegistry templateRegistry = mock( TemplateRegistry.class );
        when( templateRegistry.getParentTemplates( anyString() ) ).thenReturn( MockUtils.getParentTemplates() );

        templateManager.setTemplateRegistry( templateRegistry );


        ScriptExecutor scriptExecutor = mock( ScriptExecutor.class );
        when( scriptExecutor
                .execute( any( Agent.class ), eq( ActionType.LIST_TEMPLATES ), eq( MockUtils.MASTER_TEMPLATE_NAME ) ) )
                .thenReturn( true );

        templateManager.setScriptExecutor( scriptExecutor );

        boolean result = templateManager
                .clone( MockUtils.PHYSICAL_HOSTNAME, MockUtils.MASTER_TEMPLATE_NAME, MockUtils.LXC_HOSTNAME,
                        envId.toString() );
        assertTrue( result );
    }
}
