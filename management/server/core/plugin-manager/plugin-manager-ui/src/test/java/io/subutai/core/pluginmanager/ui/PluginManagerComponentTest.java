package io.subutai.core.pluginmanager.ui;


import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.core.pluginmanager.ui.PluginManagerComponent;
import io.subutai.core.pluginmanager.ui.PluginManagerPortalModule;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.core.pluginmanager.api.PluginManager;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;

import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class PluginManagerComponentTest
{
    private class TestPluginComponent extends PluginManagerComponent
    {
        private Component compositionRoot;


        public TestPluginComponent( final ExecutorService executorService, final PluginManagerPortalModule managerUI,
                                    final PluginManager pluginManager, final Tracker tracker )
        {
            super( executorService, managerUI, pluginManager, tracker );
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

    private TestPluginComponent testPluginComponent;

    @Mock
    PluginManager pluginManager;
    @Mock
    ExecutorService executorService;
    @Mock
    PluginManagerPortalModule portalModule;
    @Mock
    Tracker tracker;

    @Before
    public void setUp() throws Exception
    {
        testPluginComponent = new TestPluginComponent( executorService, portalModule, pluginManager, tracker );
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


    @Test
    public void testListPluginsBtnClick() throws Exception
    {
        Set<String> mySet = new HashSet<>(  );
        mySet.add( "hadoop" );
        mySet.add( "oozie" );
        when( pluginManager.getAvailablePluginNames() ).thenReturn( mySet );

        Component component = getChildComponentById( testPluginComponent, "listPluginsBtn");
        if ( component != null )
        {
            Button swapTemplatesId = (Button) component;
            swapTemplatesId.click();

//            Component component1 = getChildComponentById( testPluginComponent, "PluginsTable" );
//
//            if ( component1 != null )
//            {
//                Table tbl = (Table) component1;
//                for ( final Object item : tbl.getItemIds() )
//                {
//                    Item row = tbl.getItem( item );
//                    Property rowItemProperty = row.getItemProperty( "AVAILABLE_OPERATIONS" );
//                    HorizontalLayout availableOperations = (HorizontalLayout) rowItemProperty.getValue();
//                    Component component2 = getChildComponentById( availableOperations, "InstallButton" );
//                    if ( component2 != null )
//                    {
//                        Button installBtn = (Button) component2;
//                        installBtn.click();
//                    }
//                }
//
//            }
        }
    }
}