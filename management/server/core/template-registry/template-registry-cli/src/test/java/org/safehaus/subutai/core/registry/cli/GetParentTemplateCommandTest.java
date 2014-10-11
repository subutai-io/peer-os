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
public class GetParentTemplateCommandTest
{
    String childTemplateName;

    private TemplateRegistry templateRegistry;
    private GetParentTemplateCommand parentTemplateCommand;


    @Before
    public void setupClasses()
    {
        templateRegistry = mock( TemplateRegistry.class );
        parentTemplateCommand = new GetParentTemplateCommand();
        parentTemplateCommand.setTemplateRegistry( templateRegistry );
    }


    @Test
    public void shouldSetDifferentTemplateRegistry()
    {
        TemplateRegistry registry = mock( TemplateRegistry.class );
        parentTemplateCommand.setTemplateRegistry( registry );
        assertNotSame( templateRegistry, parentTemplateCommand.getTemplateRegistry() );
    }


    @Test( expected = NullPointerException.class )
    public void shouldThrowNullPointerExceptionOnSettingNullTemplateRegistry()
    {
        parentTemplateCommand.setTemplateRegistry( null );
    }


    @Test
    public void testParentCommandExecution() throws Exception
    {
        parentTemplateCommand.doExecute();
        verify( templateRegistry ).getParentTemplate( childTemplateName );
    }
}
