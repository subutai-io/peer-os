package org.safehaus.subutai.core.environment.terminal.ui;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.server.ui.component.ConcurrentComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;

//import org.safehaus.subutai.core.environment.api.helper.EnvironmentContainer;


/**
 * Container tree
 */
@SuppressWarnings( "serial" )

public final class EnvironmentTree extends ConcurrentComponent implements Disposable
{

    private static final Logger LOG = LoggerFactory.getLogger( UI.getCurrent().getClass().getName() );
    private final Tree tree;
    private HierarchicalContainer container;
    private Set<ContainerHost> selectedContainers = new HashSet<>();
    private Environment environment;
    private final ScheduledExecutorService scheduler;


    public EnvironmentTree( final EnvironmentManager environmentManager )
    {

        scheduler = Executors.newScheduledThreadPool( 1 );

        scheduler.scheduleWithFixedDelay( new Runnable()
        {
            public void run()
            {
                LOG.info( "Refreshing containers state..." );
                if ( environment != null )
                {
                    refreshContainers();
                }
                LOG.info( "Refreshing done." );
            }
        }, 5, 60, TimeUnit.SECONDS );
        setSizeFull();
        setMargin( true );

        BeanItemContainer<Environment> environments = new BeanItemContainer<>( Environment.class );
        environments.addAll( environmentManager.getEnvironments() );
        final ComboBox env = new ComboBox( null, environments );
        env.setItemCaptionPropertyId( "name" );
        env.setImmediate( true );
        env.setTextInputAllowed( false );
        env.setNullSelectionAllowed( false );
        Property.ValueChangeListener listener = new Property.ValueChangeListener()
        {
            public void valueChange( Property.ValueChangeEvent event )
            {
                environment = ( Environment ) event.getProperty().getValue();
                tree.setContainerDataSource( getNodeContainer() );
            }
        };

        env.addValueChangeListener( listener );

        tree = new Tree( "List of containers" );
        tree.setContainerDataSource( getNodeContainer() );
        tree.setItemIconPropertyId( "icon" );
        tree.setItemDescriptionGenerator( new AbstractSelect.ItemDescriptionGenerator()
        {

            @Override
            public String generateDescription( Component source, Object itemId, Object propertyId )
            {
                String description = "";

                Item item = tree.getItem( itemId );
                if ( item != null )
                {
                    ContainerHost ec = ( ContainerHost ) item.getItemProperty( "value" ).getValue();
                    if ( ec != null )
                    {
                        description = "Hostname: " + ec.getHostname() + "<br>" + "Peer ID: " + ec.getPeerId() + "<br>"
                                + "Agent ID: " + ec.getId();
                    }
                }

                return description;
            }
        } );
        tree.setMultiSelect( true );
        tree.setImmediate( true );
        tree.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {

                if ( event.getProperty().getValue() instanceof Set )
                {
                    Tree t = ( Tree ) event.getProperty();

                    Set<ContainerHost> selectedList = new HashSet<>();

                    for ( Object o : ( Iterable<?> ) t.getValue() )
                    {
                        if ( tree.getItem( o ).getItemProperty( "value" ).getValue() != null )
                        {
                            ContainerHost containerHost =
                                    ( ContainerHost ) tree.getItem( o ).getItemProperty( "value" ).getValue();
                            selectedList.add( containerHost );
                        }
                    }

                    selectedContainers = selectedList;
                }
            }
        } );
        GridLayout grid = new GridLayout( 1, 3 );

        grid.addComponent( new Label( "Environments:" ), 0, 0 );
        grid.addComponent( env, 0, 1 );
        grid.addComponent( tree, 0, 2 );

        addComponent( grid );
    }


    public HierarchicalContainer getNodeContainer()
    {
        container = new HierarchicalContainer();
        container.addContainerProperty( "value", ContainerHost.class, null );
        container.addContainerProperty( "icon", Resource.class, new ThemeResource( "img/lxc/physical.png" ) );

        tree.removeAllItems();
        if ( environment != null )
        {

            for ( ContainerHost ec : environment.getContainers() )
            {
                String peerId = ec.getPeerId().toString();
                String itemId = peerId + ":" + ec.getId();

                Item peer = container.getItem( peerId );
                if ( peer == null )
                {
                    peer = container.addItem( peerId );
                    container.setChildrenAllowed( peerId, true );
                    tree.setItemCaption( itemId, peerId );
                    peer.getItemProperty( "value" ).setValue( null );
                }
                Item item = container.addItem( itemId );
                container.setParent( itemId, peerId );
                container.setChildrenAllowed( itemId, false );
                tree.setItemCaption( itemId, ec.getHostname() );
                item.getItemProperty( "value" ).setValue( ec );
            }
        }
        else
        {
            LOG.info( "Environment is null" );
        }

        return container;
    }


    public Set<ContainerHost> getSelectedContainers()
    {
        return Collections.unmodifiableSet( selectedContainers );
    }


    private void refreshContainers()
    {
        for ( Object itemObj : container.getItemIds() )
        {
            String itemId = ( String ) itemObj;
            if ( itemId.indexOf( ':' ) < 0 )
            {
                continue;
            }
            Item item = container.getItem( itemId );
            Object o = item.getItemProperty( "value" ).getValue();
            if ( ( o instanceof ContainerHost ) && ( ( ( ContainerHost ) o ).isConnected() ) )
            {
                item.getItemProperty( "icon" ).setValue( new ThemeResource( "img/lxc/virtual.png" ) );
            }
            else
            {
                item.getItemProperty( "icon" ).setValue( new ThemeResource( "img/lxc/virtual-offline.png" ) );
            }
        }
    }


    public void dispose()
    {
        scheduler.shutdown();
    }
}
