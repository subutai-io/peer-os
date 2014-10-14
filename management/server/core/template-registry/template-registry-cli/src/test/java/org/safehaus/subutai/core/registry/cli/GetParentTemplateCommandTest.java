package org.safehaus.subutai.core.registry.cli;


import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.registry.api.Template;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

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


        public void setLxcArch( String lxcArch ) {this.lxcArch = lxcArch;}


        public void setChildTemplateName( String childTemplateName ) {this.childTemplateName = childTemplateName;}
    }


    @Before
    public void setupClasses()
    {
        templateRegistry = mock( TemplateRegistry.class );
        parentTemplateCommand = new GetParentTemplateCommandExt( templateRegistry );
        parentTemplateCommand.setLxcArch( MockUtils.LXC_ARCH );
        parentTemplateCommand.setChildTemplateName( MockUtils.CHILD_ONE_TEMPLATE_NAME );
        Template parentTemplate = MockUtils.PARENT_TEMPLATE;
        when( templateRegistry.getParentTemplate( MockUtils.PARENT_TEMPLATE_NAME ) ).thenReturn( parentTemplate );
        when( templateRegistry.getParentTemplate( MockUtils.PARENT_TEMPLATE_NAME, MockUtils.LXC_ARCH ) )
                .thenReturn( parentTemplate );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullRegistry() throws Exception
    {
        new GetParentTemplateCommand( null );
    }
}
