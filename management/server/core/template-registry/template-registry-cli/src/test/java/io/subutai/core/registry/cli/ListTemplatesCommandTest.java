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
 * Test for ListTemplatesCommand
 */
public class ListTemplatesCommandTest extends TestParent
{
    private TemplateRegistry templateRegistry;
    private ListTemplatesCommandExt listTemplatesCommand;


    static class ListTemplatesCommandExt extends ListTemplatesCommand
    {
        ListTemplatesCommandExt( final TemplateRegistry templateRegistry )
        {
            super( templateRegistry );
        }


        public void setLxcArch( String lxcArch )
        {
            this.lxcArch = lxcArch;
        }
    }


    @Before
    public void setUp()
    {
        templateRegistry = mock( TemplateRegistry.class );
        listTemplatesCommand = new ListTemplatesCommandExt( templateRegistry );
        listTemplatesCommand.setLxcArch( MockUtils.LXC_ARCH );
        List<Template> allTemplates = Lists.newArrayList( MockUtils.CHILD_TEMPLATE_ONE, MockUtils.CHILD_TEMPLATE_TWO,
                MockUtils.PARENT_TEMPLATE );
        when( templateRegistry.getAllTemplates() ).thenReturn( allTemplates );
        when( templateRegistry.getAllTemplates( MockUtils.LXC_ARCH ) ).thenReturn( allTemplates );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullRegistry() throws Exception
    {
        new ListTemplatesCommand( null );
    }


    @Test
    public void testPrint() throws Exception
    {
        listTemplatesCommand.doExecute();

        assertTrue( getSysOut().contains( MockUtils.CHILD_TWO_TEMPLATE_NAME ) );
        assertTrue( getSysOut().contains( MockUtils.CHILD_ONE_TEMPLATE_NAME ) );
        assertTrue( getSysOut().contains( MockUtils.PARENT_TEMPLATE_NAME ) );
    }


    @Test
    public void testPrintNullArch() throws Exception
    {
        listTemplatesCommand.setLxcArch( null );
        listTemplatesCommand.doExecute();

        assertTrue( getSysOut().contains( MockUtils.CHILD_TWO_TEMPLATE_NAME ) );
        assertTrue( getSysOut().contains( MockUtils.CHILD_ONE_TEMPLATE_NAME ) );
        assertTrue( getSysOut().contains( MockUtils.PARENT_TEMPLATE_NAME ) );
    }


    @Test
    public void shouldPrint2SysOut() throws Exception
    {
        when( templateRegistry.getAllTemplates() ).thenReturn( Collections.<Template>emptyList() );
        when( templateRegistry.getAllTemplates( MockUtils.LXC_ARCH ) ).thenReturn( Collections.<Template>emptyList() );

        listTemplatesCommand.doExecute();


        assertTrue( getSysOut().isEmpty() );
    }
}
