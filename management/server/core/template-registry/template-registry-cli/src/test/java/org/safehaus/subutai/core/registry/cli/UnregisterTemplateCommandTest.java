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
public class UnregisterTemplateCommandTest
{
    String templateName;

    private TemplateRegistry templateRegistry;
    private UnregisterTemplateCommand unregisterTemplateCommand;


    @Before
    public void setupClasses()
    {
        templateRegistry = mock( TemplateRegistry.class );
        unregisterTemplateCommand = new UnregisterTemplateCommand();
        unregisterTemplateCommand.setTemplateRegistry( templateRegistry );
    }


    @Test
    public void shouldSetNewTemplateRegistryValue()
    {
        TemplateRegistry registry = mock( TemplateRegistry.class );
        unregisterTemplateCommand.setTemplateRegistry( registry );
        assertNotSame( templateRegistry, unregisterTemplateCommand.getTemplateRegistry() );
    }


    @Test( expected = NullPointerException.class )
    public void shouldThrowExceptionOnSettingNullTemplateRegistry()
    {
        unregisterTemplateCommand.setTemplateRegistry( null );
    }


    @Test
    public void shouldAccessTemplateRegistryAndCallUnregisteredTemplateMethod() throws Exception
    {
        unregisterTemplateCommand.doExecute();
        verify( templateRegistry ).unregisterTemplate( templateName );
    }


    protected Object doExecute() throws Exception
    {

        templateRegistry.unregisterTemplate( templateName );

        System.out.println( String.format( "Template %s unregistered successfully", templateName ) );


        return null;
    }
}
