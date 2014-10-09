package org.safehaus.subutai.core.registry.cli;


import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.registry.api.Template;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Created by talas on 10/2/14.
 */
public class GetChildTemplatesCommandTest
{
    String parentTemplateName;
    String lxcArch;


    private GetChildTemplatesCommand templatesCommand;
    private TemplateRegistry templateRegistry;


    @Before
    public void setupClasses()
    {
        templateRegistry = mock( TemplateRegistry.class );
        when( templateRegistry.getChildTemplates( parentTemplateName ) )
                .thenReturn( Collections.<Template>emptyList() );
        when( templateRegistry.getChildTemplates( parentTemplateName, lxcArch ) )
                .thenReturn( Collections.<Template>emptyList() );
        templatesCommand = new GetChildTemplatesCommand();
        templatesCommand.setTemplateRegistry( templateRegistry );
    }


    @Test
    public void shouldSetTemplateRegistry()
    {
        TemplateRegistry registry = mock( TemplateRegistry.class );
        templatesCommand.setTemplateRegistry( registry );
        assertNotSame( templateRegistry, templatesCommand.getTemplateRegistry() );
    }


    @Test( expected = NullPointerException.class )
    public void shouldHandleNullPointerExceptionOnNullTemplateRegistrySetting()
    {
        templatesCommand.setTemplateRegistry( null );
    }


    @Test
    public void shouldExecuteCommand() throws Exception
    {
        templatesCommand.doExecute();
        verify( templateRegistry ).getChildTemplates( parentTemplateName );
    }
}
