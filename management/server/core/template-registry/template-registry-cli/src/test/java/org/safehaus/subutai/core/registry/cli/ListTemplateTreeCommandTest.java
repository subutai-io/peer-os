package org.safehaus.subutai.core.registry.cli;


import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.registry.api.Template;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.registry.api.TemplateTree;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for ListTemplateTreeCommand
 */
public class ListTemplateTreeCommandTest extends TestParent
{
    private TemplateRegistry templateRegistry;
    private ListTemplateTreeCommand templateTreeCommand;


    @Before
    public void setUp()
    {
        templateRegistry = mock( TemplateRegistry.class );
        templateTreeCommand = new ListTemplateTreeCommand( templateRegistry );
        List<Template> rootTemplates = Lists.newArrayList( MockUtils.PARENT_TEMPLATE );
        List<Template> childTemplates =
                Lists.newArrayList( MockUtils.CHILD_TEMPLATE_TWO, MockUtils.CHILD_TEMPLATE_ONE );

        TemplateTree templateTree = mock( TemplateTree.class );
        when( templateRegistry.getTemplateTree() ).thenReturn( templateTree );
        when( templateTree.getRootTemplates() ).thenReturn( rootTemplates );
        when( templateTree.getChildrenTemplates( MockUtils.PARENT_TEMPLATE ) ).thenReturn( childTemplates );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullRegistry() throws Exception
    {
        new ListTemplateTreeCommand( null );
    }


    @Test
    public void testPrint() throws Exception
    {
        templateTreeCommand.doExecute();

        assertTrue( getSysOut().contains( MockUtils.CHILD_TWO_TEMPLATE_NAME ) );
        assertTrue( getSysOut().contains( MockUtils.CHILD_ONE_TEMPLATE_NAME ) );
        assertTrue( getSysOut().contains( MockUtils.PARENT_TEMPLATE_NAME ) );
    }
}
