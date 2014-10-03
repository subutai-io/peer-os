package org.safehaus.subutai.core.registry.cli;


import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.registry.api.TemplateTree;

import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Created by talas on 10/2/14.
 */
public class ListTemplateTreeCommandTest
{
    private TemplateRegistry templateRegistry;
    private ListTemplateTreeCommand templateTreeCommand;


    @Before
    public void setupClasses()
    {
        templateRegistry = mock( TemplateRegistry.class );
        templateTreeCommand = new ListTemplateTreeCommand();
        templateTreeCommand.setTemplateRegistry( templateRegistry );
    }


    @Test( expected = NullPointerException.class )
    public void shouldThrowExceptionForSettingNullTemplateRegistry()
    {
        templateTreeCommand.setTemplateRegistry( null );
    }


    @Test
    public void shouldSetNewTemplateRegistryValue()
    {
        TemplateRegistry registry = mock( TemplateRegistry.class );
        templateTreeCommand.setTemplateRegistry( registry );
        assertNotSame( templateRegistry, templateTreeCommand.getTemplateRegistry() );
    }


    @Test
    public void shouldAccessTemplateRegistryAndCallGetTemplateTree() throws Exception
    {
        when( templateRegistry.getTemplateTree() ).thenReturn( new TemplateTree() );
        templateTreeCommand.doExecute();
        verify( templateRegistry ).getTemplateTree();
    }
}
