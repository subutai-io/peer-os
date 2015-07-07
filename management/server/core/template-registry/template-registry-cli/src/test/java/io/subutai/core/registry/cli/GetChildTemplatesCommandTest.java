package io.subutai.core.registry.cli;


import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import io.subutai.common.protocol.Template;
import io.subutai.core.registry.api.TemplateRegistry;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for GetChildTemplatesCommand
 */
public class GetChildTemplatesCommandTest extends TestParent
{


    private GetChildTemplatesCommandExt templatesCommand;
    private TemplateRegistry templateRegistry;


    static class GetChildTemplatesCommandExt extends GetChildTemplatesCommand
    {

        GetChildTemplatesCommandExt( final TemplateRegistry templateRegistry )
        {
            super( templateRegistry );
        }


        public void setLxcArch( String lxcArch )
        {
            this.lxcArch = lxcArch;
        }


        public void setParentTemplateName( String parentTemplateName1 )
        {
            this.parentTemplateName = parentTemplateName1;
        }
    }


    @Before
    public void setUp()
    {


        templateRegistry = mock( TemplateRegistry.class );

        templatesCommand = new GetChildTemplatesCommandExt( templateRegistry );
        templatesCommand.setLxcArch( MockUtils.LXC_ARCH );
        templatesCommand.setParentTemplateName( MockUtils.PARENT_TEMPLATE_NAME );

        List<Template> childTemplates = MockUtils.getChildTemplates();

        when( templateRegistry.getChildTemplates( MockUtils.PARENT_TEMPLATE_NAME ) ).thenReturn( childTemplates );
        when( templateRegistry.getChildTemplates( MockUtils.PARENT_TEMPLATE_NAME, MockUtils.LXC_ARCH ) )
                .thenReturn( childTemplates );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullRegistry() throws Exception
    {
        new GetChildTemplatesCommand( null );
    }


    @Test
    public void testPrint() throws Exception
    {

        templatesCommand.doExecute();

        assertTrue( getSysOut().contains( MockUtils.CHILD_ONE_TEMPLATE_NAME ) );
        assertTrue( getSysOut().contains( MockUtils.CHILD_TWO_TEMPLATE_NAME ) );
    }


    @Test
    public void testNullLxcArch() throws Exception
    {
        templatesCommand.setLxcArch( null );

        templatesCommand.doExecute();

        assertTrue( getSysOut().contains( MockUtils.CHILD_ONE_TEMPLATE_NAME ) );
        assertTrue( getSysOut().contains( MockUtils.CHILD_TWO_TEMPLATE_NAME ) );
    }


    @Test
    public void shouldPrint2SysOut() throws Exception
    {
        when( templateRegistry.getChildTemplates( MockUtils.PARENT_TEMPLATE_NAME ) )
                .thenReturn( Collections.<Template>emptyList() );
        when( templateRegistry.getChildTemplates( MockUtils.PARENT_TEMPLATE_NAME, MockUtils.LXC_ARCH ) )
                .thenReturn( Collections.<Template>emptyList() );

        templatesCommand.doExecute();


        assertTrue( getSysOut().contains( MockUtils.PARENT_TEMPLATE_NAME ) );
    }
}
