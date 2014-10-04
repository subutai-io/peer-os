package org.safehaus.subutai.core.registry.cli;


import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Created by talas on 10/2/14.
 */
public class ListTemplatesCommandTest
{
    String lxcArch;
    private TemplateRegistry templateRegistry;
    private ListTemplatesCommand listTemplatesCommand;


    @Before
    public void setupClasses()
    {
        templateRegistry = mock( TemplateRegistry.class );
        listTemplatesCommand = new ListTemplatesCommand();
        listTemplatesCommand.setTemplateRegistry( templateRegistry );
    }


    @Test( expected = NullPointerException.class )
    public void shouldThrowExceptionOnSettingNull()
    {
        listTemplatesCommand.setTemplateRegistry( null );
    }


    @Test
    public void shouldSetNewTemplateRegistry()
    {
        TemplateRegistry registry = mock( TemplateRegistry.class );
        listTemplatesCommand.setTemplateRegistry( registry );
        assertNotSame( templateRegistry, registry );
    }


    @Test
    public void shouldAccessTemplateRegistryAndCallGetAllTemplatesMethod() throws Exception
    {
        listTemplatesCommand.doExecute();
        verify( templateRegistry ).getAllTemplates();
    }
}
