package io.subutai.core.registry.cli;


import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Template;
import io.subutai.core.registry.api.TemplateRegistry;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for GetParentTemplatesCommand
 */
public class GetParentTemplatesCommandTest extends TestParent
{


    private TemplateRegistry templateRegistry;
    private GetParentTemplatesCommandExt parentTemplatesCommand;


    static class GetParentTemplatesCommandExt extends GetParentTemplatesCommand
    {

        GetParentTemplatesCommandExt( final TemplateRegistry templateRegistry )
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
        parentTemplatesCommand = new GetParentTemplatesCommandExt( templateRegistry );
        parentTemplatesCommand.setLxcArch( MockUtils.LXC_ARCH );
        parentTemplatesCommand.setChildTemplateName( MockUtils.CHILD_ONE_TEMPLATE_NAME );
        List<Template> parentTemplates = Lists.newArrayList( MockUtils.PARENT_TEMPLATE );
        when( templateRegistry.getParentTemplates( MockUtils.CHILD_ONE_TEMPLATE_NAME ) ).thenReturn( parentTemplates );
        when( templateRegistry.getParentTemplates( MockUtils.CHILD_ONE_TEMPLATE_NAME, MockUtils.LXC_ARCH ) )
                .thenReturn( parentTemplates );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullRegistry() throws Exception
    {
        new GetParentTemplatesCommand( null );
    }


    @Test
    public void testPrint() throws Exception
    {
        parentTemplatesCommand.doExecute();

        assertTrue( getSysOut().contains( MockUtils.PARENT_TEMPLATE_NAME ) );
    }


    @Test
    public void testNullLxcArch() throws Exception
    {
        parentTemplatesCommand.setLxcArch( null );

        parentTemplatesCommand.doExecute();

        assertTrue( getSysOut().contains( MockUtils.PARENT_TEMPLATE_NAME ) );
    }


    @Test
    public void shouldPrint2SysOut() throws Exception
    {
        when( templateRegistry.getParentTemplates( MockUtils.CHILD_ONE_TEMPLATE_NAME ) )
                .thenReturn( Collections.<Template>emptyList() );
        when( templateRegistry.getParentTemplates( MockUtils.CHILD_ONE_TEMPLATE_NAME, MockUtils.LXC_ARCH ) )
                .thenReturn( Collections.<Template>emptyList() );

        parentTemplatesCommand.doExecute();


        assertTrue( getSysOut().contains( MockUtils.CHILD_ONE_TEMPLATE_NAME ) );
    }
}
