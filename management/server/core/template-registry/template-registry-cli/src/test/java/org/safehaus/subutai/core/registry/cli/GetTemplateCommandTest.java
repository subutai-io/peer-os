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
public class GetTemplateCommandTest
{
    String templateName;
    String lxcArch;


    private TemplateRegistry templateRegistry;
    private GetTemplateCommand templateCommand;


    @Before
    public void setupClasses()
    {
        templateRegistry = mock( TemplateRegistry.class );
        templateCommand = new GetTemplateCommand();
        templateCommand.setTemplateRegistry( templateRegistry );
    }


    @Test( expected = NullPointerException.class )
    public void shouldThrowNullPointerExceptionOnSettingTemplateRegistry()
    {
        templateCommand.setTemplateRegistry( null );
    }


    @Test
    public void shouldSetNewTemplateRegistryOnSetTemplateRegistryCall()
    {
        TemplateRegistry registry = mock( TemplateRegistry.class );
        templateCommand.setTemplateRegistry( registry );
        assertNotSame( templateRegistry, templateCommand.getTemplateRegistry() );
    }


    @Test
    public void shouldAccessTemplateRegistryAndCallGetTemplate() throws Exception
    {
        templateCommand.doExecute();
        verify( templateRegistry ).getTemplate( templateName );
    }
}
