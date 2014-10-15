package org.safehaus.subutai.core.registry.api;


import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.protocol.Template;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


/**
 * Test for TemplateTree
 */
@RunWith( MockitoJUnitRunner.class )
public class TemplateTreeTest
{
    private static final String LXC_ARCH = "amd64";
    private static final String PARENT_TEMPLATE_NAME = "parent";
    private static final String CHILD_ONE_TEMPLATE_NAME = "child1";
    private static final String CHILD_TWO_TEMPLATE_NAME = "child2";

    @Mock
    private Template PARENT_TEMPLATE;

    @Mock
    private Template CHILD_TEMPLATE_ONE;
    @Mock
    private Template CHILD_TEMPLATE_TWO;

    private TemplateTree templateTree = new TemplateTree();


    @Before
    public void setUp() throws Exception
    {

        when( PARENT_TEMPLATE.getTemplateName() ).thenReturn( PARENT_TEMPLATE_NAME );
        when( PARENT_TEMPLATE.getLxcArch() ).thenReturn( LXC_ARCH );
        when( CHILD_TEMPLATE_ONE.getParentTemplateName() ).thenReturn( PARENT_TEMPLATE_NAME );
        when( CHILD_TEMPLATE_ONE.getTemplateName() ).thenReturn( CHILD_ONE_TEMPLATE_NAME );
        when( CHILD_TEMPLATE_ONE.getLxcArch() ).thenReturn( LXC_ARCH );
        when( CHILD_TEMPLATE_TWO.getParentTemplateName() ).thenReturn( PARENT_TEMPLATE_NAME );
        when( CHILD_TEMPLATE_TWO.getTemplateName() ).thenReturn( CHILD_TWO_TEMPLATE_NAME );
        when( CHILD_TEMPLATE_TWO.getLxcArch() ).thenReturn( LXC_ARCH );

        templateTree.addTemplate( PARENT_TEMPLATE );
        templateTree.addTemplate( CHILD_TEMPLATE_ONE );
        templateTree.addTemplate( CHILD_TEMPLATE_TWO );
    }


    @Test( expected = NullPointerException.class )
    public void testAddTemplateShouldFailOnNullTemplate() throws Exception
    {
        templateTree.addTemplate( null );
    }


    @Test
    public void testGetParentTemplate() throws Exception
    {

        Template parent1 = templateTree.getParentTemplate( CHILD_ONE_TEMPLATE_NAME, LXC_ARCH );
        Template parent2 = templateTree.getParentTemplate( CHILD_TWO_TEMPLATE_NAME, LXC_ARCH );
        Template parent3 = templateTree.getParentTemplate( CHILD_TEMPLATE_TWO );

        assertEquals( PARENT_TEMPLATE, parent1 );
        assertEquals( parent2, parent1 );
        assertEquals( parent3, parent1 );
    }


    @Test
    public void testGetParentTemplateName() throws Exception
    {
        assertEquals( PARENT_TEMPLATE_NAME, templateTree.getParentTemplateName( CHILD_ONE_TEMPLATE_NAME, LXC_ARCH ) );
    }


    @Test
    public void testGetChildrenTemplates() throws Exception
    {
        List<Template> expected = Lists.newArrayList( CHILD_TEMPLATE_ONE, CHILD_TEMPLATE_TWO );
        List<Template> children = templateTree.getChildrenTemplates( PARENT_TEMPLATE_NAME, LXC_ARCH );
        List<Template> children2 = templateTree.getChildrenTemplates( PARENT_TEMPLATE );

        assertEquals( expected, children2 );
        assertEquals( children, children2 );
    }


    @Test
    public void testGetRootTemplates() throws Exception
    {
        List<Template> expected = Lists.newArrayList( PARENT_TEMPLATE );

        List<Template> rootTemplates = templateTree.getRootTemplates();

        assertEquals( expected, rootTemplates );
    }
}
