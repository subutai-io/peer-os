package io.subutai.core.registry.cli;


import java.util.List;

import org.safehaus.subutai.common.protocol.Template;

import com.google.common.collect.Lists;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Mock Utils
 */
public class MockUtils
{
    public static Template PARENT_TEMPLATE = mock( Template.class );
    public static Template CHILD_TEMPLATE_ONE = mock( Template.class );
    public static Template CHILD_TEMPLATE_TWO = mock( Template.class );
    public static final String LXC_ARCH = "amd64";
    public static final String PARENT_TEMPLATE_NAME = "parent";
    public static final String CHILD_ONE_TEMPLATE_NAME = "child1";
    public static final String CHILD_TWO_TEMPLATE_NAME = "child2";


    static
    {

        when( PARENT_TEMPLATE.getTemplateName() ).thenReturn( PARENT_TEMPLATE_NAME );
        when( PARENT_TEMPLATE.getLxcArch() ).thenReturn( LXC_ARCH );
        when( CHILD_TEMPLATE_ONE.getParentTemplateName() ).thenReturn( PARENT_TEMPLATE_NAME );
        when( CHILD_TEMPLATE_ONE.getTemplateName() ).thenReturn( CHILD_ONE_TEMPLATE_NAME );
        when( CHILD_TEMPLATE_ONE.getLxcArch() ).thenReturn( LXC_ARCH );
        when( CHILD_TEMPLATE_TWO.getParentTemplateName() ).thenReturn( PARENT_TEMPLATE_NAME );
        when( CHILD_TEMPLATE_TWO.getTemplateName() ).thenReturn( CHILD_TWO_TEMPLATE_NAME );
        when( CHILD_TEMPLATE_TWO.getLxcArch() ).thenReturn( LXC_ARCH );
        when( CHILD_TEMPLATE_ONE.toString() ).thenReturn( CHILD_ONE_TEMPLATE_NAME );
        when( CHILD_TEMPLATE_TWO.toString() ).thenReturn( CHILD_TWO_TEMPLATE_NAME );
        when( PARENT_TEMPLATE.toString() ).thenReturn( PARENT_TEMPLATE_NAME );
        when( PARENT_TEMPLATE.getChildren() ).thenReturn( getChildTemplates() );
    }


    public static List<Template> getChildTemplates()
    {
        return Lists.newArrayList( CHILD_TEMPLATE_ONE, CHILD_TEMPLATE_TWO );
    }
}
