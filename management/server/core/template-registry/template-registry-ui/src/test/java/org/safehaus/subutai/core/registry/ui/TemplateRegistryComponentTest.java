package org.safehaus.subutai.core.registry.ui;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import com.google.common.collect.Lists;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;

import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class TemplateRegistryComponentTest
{

    private class TestTemplateRegistryComponent extends TemplateRegistryComponent
    {
        private Component compositionRoot;


        public TestTemplateRegistryComponent( final TemplateRegistry registryManager )
        {
            super( registryManager );
        }


        @Override
        public Component getCompositionRoot()
        {
            return super.getCompositionRoot();
        }


        @Override
        public void setCompositionRoot( final Component compositionRoot )
        {
            super.setCompositionRoot( compositionRoot );
        }
    }


    @Mock
    private TemplateRegistry templateRegistry;

    private TestTemplateRegistryComponent templateRegistryComponent;


    @Before
    public void setUp() throws Exception
    {
        when( templateRegistry.getAllTemplates() ).thenReturn( Lists.<Template>newArrayList() );

        templateRegistryComponent = new TestTemplateRegistryComponent( templateRegistry );
    }


    @Test
    public void testCreateTreeContent() throws Exception
    {

    }


    @Test
    public void testSwapTemplatesButtonClick() throws Exception
    {
        Component component = getChildComponentById( templateRegistryComponent, "swapTemplatesId");
        if ( component != null )
        {
            Button swapTemplatesId = (Button) component;
            swapTemplatesId.click();
        }
    }


    private Component getChildComponentById( HasComponents root, String id )
    {
        for ( Component component : root )
        {
            if ( id.equals( component.getId() ) )
            {
                return component;
            }
            if ( component instanceof HasComponents )
            {
                Component temp = getChildComponentById( ( HasComponents ) component, id );
                if ( temp != null )
                {
                    return temp;
                }
            }
        }
        return null;
    }
}