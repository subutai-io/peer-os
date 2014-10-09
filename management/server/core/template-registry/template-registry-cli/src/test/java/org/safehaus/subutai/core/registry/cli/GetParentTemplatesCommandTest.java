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
public class GetParentTemplatesCommandTest
{
    String childTemplateName;
    String lxcArch;

    private TemplateRegistry templateRegistry;
    private GetParentTemplatesCommand parentTemplatesCommand;


    @Before
    public void setupClasses()
    {
        templateRegistry = mock( TemplateRegistry.class );
        parentTemplatesCommand = new GetParentTemplatesCommand();
        parentTemplatesCommand.setTemplateRegistry( templateRegistry );
    }


    @Test
    public void shouldSetNewTemplateRegistry()
    {
        TemplateRegistry registry = mock( TemplateRegistry.class );
        parentTemplatesCommand.setTemplateRegistry( registry );
        assertNotSame( templateRegistry, parentTemplatesCommand.getTemplateRegistry() );
    }


    @Test( expected = NullPointerException.class )
    public void shouldFailOnSettingNullTemplateRegistry()
    {
        parentTemplatesCommand.setTemplateRegistry( null );
    }


    @Test
    public void shouldAccessTemplateRegistryAndGetParentTemplates() throws Exception
    {
        parentTemplatesCommand.doExecute();
        verify( templateRegistry ).getParentTemplates( childTemplateName );
    }
}
