package io.subutai.core.registry.cli;


import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Template;
import io.subutai.core.registry.api.TemplateRegistry;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for GetTemplateCommand
 */
public class GetTemplateCommandTest extends TestParent
{


    private TemplateRegistry templateRegistry;
    private GetTemplateCommandExt templateCommand;


    static class GetTemplateCommandExt extends GetTemplateCommand
    {
        GetTemplateCommandExt( final TemplateRegistry templateRegistry )
        {
            super( templateRegistry );
        }


        public void setLxcArch( String lxcArch )
        {
            this.lxcArch = lxcArch;
        }


        public void setTemplateName( String templateName )
        {
            this.templateName = templateName;
        }
    }


    @Before
    public void setUp()
    {
        templateRegistry = mock( TemplateRegistry.class );
        templateCommand = new GetTemplateCommandExt( templateRegistry );
        templateCommand.setLxcArch( MockUtils.LXC_ARCH );
        templateCommand.setTemplateName( MockUtils.CHILD_TWO_TEMPLATE_NAME );
        Template childTemplate = MockUtils.CHILD_TEMPLATE_TWO;
        when( templateRegistry.getTemplate( MockUtils.CHILD_TWO_TEMPLATE_NAME ) ).thenReturn( childTemplate );
        when( templateRegistry.getTemplate( MockUtils.CHILD_TWO_TEMPLATE_NAME, MockUtils.LXC_ARCH ) )
                .thenReturn( childTemplate );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullRegistry() throws Exception
    {
        new GetTemplateCommand( null );
    }


    @Test
    public void testPrint() throws Exception
    {
        templateCommand.doExecute();

        assertTrue( getSysOut().contains( MockUtils.CHILD_TWO_TEMPLATE_NAME ) );
    }


    @Test
    public void testNullLxcArch() throws Exception
    {
        templateCommand.setLxcArch( null );

        templateCommand.doExecute();

        assertTrue( getSysOut().contains( MockUtils.CHILD_TWO_TEMPLATE_NAME ) );
    }


    @Test
    public void shouldPrint2SysOut() throws Exception
    {
        when( templateRegistry.getTemplate( MockUtils.CHILD_TWO_TEMPLATE_NAME ) ).thenReturn( null );
        when( templateRegistry.getTemplate( MockUtils.CHILD_TWO_TEMPLATE_NAME, MockUtils.LXC_ARCH ) )
                .thenReturn( null );

        templateCommand.doExecute();


        assertTrue( getSysOut().contains( MockUtils.CHILD_TWO_TEMPLATE_NAME ) );
    }
}
