package io.subutai.core.registry.cli;


import org.junit.Before;
import org.junit.Test;
import io.subutai.core.registry.api.TemplateRegistry;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Test or UnregisterTemplateCommand
 */
public class UnregisterTemplateCommandTest extends TestParent
{

    private TemplateRegistry templateRegistry;
    private UnregisterTemplateCommand unregisterTemplateCommand;


    @Before
    public void setUp()
    {
        templateRegistry = mock( TemplateRegistry.class );
        unregisterTemplateCommand = new UnregisterTemplateCommand( templateRegistry );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullRegistry()
    {
        new UnregisterTemplateCommand( null );
    }


    @Test
    public void testUnregisterTemplate() throws Exception
    {

        unregisterTemplateCommand.doExecute();

        verify( templateRegistry ).unregisterTemplate( anyString() );
        assertTrue( getSysOut().contains( "success" ) );
    }
}
