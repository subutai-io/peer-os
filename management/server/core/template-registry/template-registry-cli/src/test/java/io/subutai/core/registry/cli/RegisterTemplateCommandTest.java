package io.subutai.core.registry.cli;


import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.subutai.core.registry.api.TemplateRegistry;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Test for RegisterTemplateCommand
 */
public class RegisterTemplateCommandTest extends TestParent
{


    private static final String FILE_PATH = "test-file";
    private TemplateRegistry templateRegistry;
    private RegisterTemplateCommandExt registerTemplateCommand;
    private final File file = new File( FILE_PATH );


    static class RegisterTemplateCommandExt extends RegisterTemplateCommand
    {
        RegisterTemplateCommandExt( final TemplateRegistry templateRegistry )
        {
            super( templateRegistry );
        }


        public void setConfigFilePath( String configFilePath )
        {
            this.configFilePath = configFilePath;
        }


        public void setPackagesFilePath( String packagesFilePath )
        {
            this.packagesFilePath = packagesFilePath;
        }


        public void setMd5sum( String md5sum )
        {
            this.md5sum = md5sum;
        }
    }


    @Before
    public void setUp() throws IOException
    {
        templateRegistry = mock( TemplateRegistry.class );
        registerTemplateCommand = new RegisterTemplateCommandExt( templateRegistry );
        registerTemplateCommand.setConfigFilePath( FILE_PATH );
        registerTemplateCommand.setPackagesFilePath( FILE_PATH );
        file.createNewFile();
    }


    @After
    public void tearDown()
    {

        file.delete();
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullRegistry()
    {
        new RegisterTemplateCommand( null );
    }


    @Test
    public void testRegisterTemplate() throws Exception
    {
        registerTemplateCommand.doExecute();

        verify( templateRegistry ).registerTemplate( anyString(), anyString(), anyString() );
        assertTrue( getSysOut().contains( "success" ) );
    }
}
