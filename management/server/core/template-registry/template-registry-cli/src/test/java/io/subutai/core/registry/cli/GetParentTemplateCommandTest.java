package io.subutai.core.registry.cli;


import org.junit.Before;
import org.junit.Test;
import io.subutai.common.protocol.Template;
import io.subutai.core.registry.api.TemplateRegistry;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for GetParentTemplateCommand
 */
public class GetParentTemplateCommandTest extends TestParent
{

    private TemplateRegistry templateRegistry;
    private GetParentTemplateCommandExt parentTemplateCommand;


    static class GetParentTemplateCommandExt extends GetParentTemplateCommand
    {

        GetParentTemplateCommandExt( final TemplateRegistry templateRegistry )
        {
            super( templateRegistry );
        }


        public void setLxcArch( String lxcArch )
        {
            this.lxcArch = lxcArch;
        }


        public void setChildTemplateName( String childTemplateName )
        {
            this.childTemplateName = childTemplateName;
        }
    }


    @Before
    public void setUp()
    {
        templateRegistry = mock( TemplateRegistry.class );
        parentTemplateCommand = new GetParentTemplateCommandExt( templateRegistry );
        parentTemplateCommand.setLxcArch( MockUtils.LXC_ARCH );
        parentTemplateCommand.setChildTemplateName( MockUtils.CHILD_ONE_TEMPLATE_NAME );
        Template parentTemplate = MockUtils.PARENT_TEMPLATE;
        when( templateRegistry.getParentTemplate( MockUtils.CHILD_ONE_TEMPLATE_NAME ) ).thenReturn( parentTemplate );
        when( templateRegistry.getParentTemplate( MockUtils.CHILD_ONE_TEMPLATE_NAME, MockUtils.LXC_ARCH ) )
                .thenReturn( parentTemplate );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullRegistry() throws Exception
    {
        new GetParentTemplateCommand( null );
    }


    @Test
    public void testPrint() throws Exception
    {
        parentTemplateCommand.doExecute();

        assertTrue( getSysOut().contains( MockUtils.PARENT_TEMPLATE_NAME ) );
    }


    @Test
    public void testNullLxcArch() throws Exception
    {
        parentTemplateCommand.setLxcArch( null );

        parentTemplateCommand.doExecute();

        assertTrue( getSysOut().contains( MockUtils.PARENT_TEMPLATE_NAME ) );
    }


    @Test
    public void shouldPrint2SysOut() throws Exception
    {
        when( templateRegistry.getParentTemplate( MockUtils.CHILD_ONE_TEMPLATE_NAME ) ).thenReturn( null );
        when( templateRegistry.getParentTemplate( MockUtils.CHILD_ONE_TEMPLATE_NAME, MockUtils.LXC_ARCH ) )
                .thenReturn( null );

        parentTemplateCommand.doExecute();


        assertTrue( getSysOut().contains( MockUtils.CHILD_ONE_TEMPLATE_NAME ) );
    }
}
