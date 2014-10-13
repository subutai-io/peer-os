package org.safehaus.subutai.core.registry.cli;


import java.nio.charset.Charset;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.mock;


/**
 * Created by talas on 10/2/14.
 */
public class RegisterTemplateCommandTest
{
    String configFilePath;
    String packagesFilePath;
    String md5sum;

    private TemplateRegistry templateRegistry;
    private RegisterTemplateCommand registerTemplateCommand;


    @Before
    public void setupClasses()
    {
        templateRegistry = mock( TemplateRegistry.class );
        registerTemplateCommand = new RegisterTemplateCommand();
        registerTemplateCommand.setTemplateRegistry( templateRegistry );
    }


    @Test( expected = NullPointerException.class )
    public void shouldThrowNullPointerExceptionOnSettingNullObject()
    {
        registerTemplateCommand.setTemplateRegistry( null );
    }


    @Test
    public void shouldSetNewTemplateRegistry()
    {
        TemplateRegistry registry = mock( TemplateRegistry.class );
        registerTemplateCommand.setTemplateRegistry( registry );
        assertNotSame( templateRegistry, registerTemplateCommand.getTemplateRegistry() );
    }

    //    @Ignore
    //    @Test
    //    public void shouldAccessTemplateRegistryAndCallRegisterTemplateMethod() throws Exception
    //    {
    //        when( templateRegistry.registerTemplate( configFilePath, packagesFilePath, md5sum ) ).thenReturn( true );
    //        registerTemplateCommand.doExecute();
    //        verify( templateRegistry ).registerTemplate( configFilePath, packagesFilePath, md5sum );
    //    }


    protected Object doExecute() throws Exception
    {

        templateRegistry.registerTemplate( FileUtil.readFile( configFilePath, Charset.defaultCharset() ),
                FileUtil.readFile( packagesFilePath, Charset.defaultCharset() ), md5sum, UUID.randomUUID() );

        System.out.println( "Template registered successfully" );

        return null;
    }
}
