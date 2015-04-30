package org.safehaus.subutai.core.environment.terminal.ui;


import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.environment.EnvironmentNotFoundException;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.server.ui.component.ConcurrentComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.vaadin.data.Container;
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
import com.vaadin.ui.Notification;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;


/**
 * Container tree
 */
@SuppressWarnings( "serial" )

public final class EnvironmentTree extends ConcurrentComponent
{

    private static final Logger LOG = LoggerFactory.getLogger( UI.getCurrent().getClass().getName() );
    private static final String VALUE_PROPERTY = "value";
    private static final String ICON_PROPERTY = "icon";
    private final Tree tree;
    private HierarchicalContainer container;
    private Set<ContainerHost> selectedContainers = new HashSet<>();
    private Environment environment;
    private ScheduledExecutorService scheduler;
    private final EnvironmentManager environmentManager;
    private final ComboBox envCombo;
    private final Date updateDate;


    public EnvironmentTree( final EnvironmentManager environmentManager, final Date updateDate )
    {

        this.environmentManager = environmentManager;
        this.updateDate = new Date( updateDate.getTime() );

        setSizeFull();
        setMargin( true );

        BeanItemContainer<Environment> environments = new BeanItemContainer<>( Environment.class );
        environments.addAll( environmentManager.getEnvironments() );
        envCombo = new ComboBox( null, environments );

        envCombo.setItemCaptionPropertyId( "name" );
        envCombo.setImmediate( true );
        envCombo.setTextInputAllowed( false );
        envCombo.setNullSelectionAllowed( false );
        Property.ValueChangeListener listener = new Property.ValueChangeListener()
        {
            public void valueChange( Property.ValueChangeEvent event )
            {
                environment = ( Environment ) event.getProperty().getValue();
                tree.setContainerDataSource( getNodeContainer() );
                refreshContainers();
            }
        };
        envCombo.addValueChangeListener( listener );

        tree = new Tree( "List of containers" );
        tree.setContainerDataSource( getNodeContainer() );
        tree.setItemIconPropertyId( ICON_PROPERTY );
        tree.setItemDescriptionGenerator( new AbstractSelect.ItemDescriptionGenerator()
        {
            @Override
            public String generateDescription( Component source, Object itemId, Object propertyId )
            {
                String description = "";
                Item item = tree.getItem( itemId );
                if ( item != null && item.getItemProperty( VALUE_PROPERTY ) != null )
                {
                    ContainerHost ec = ( ContainerHost ) item.getItemProperty( VALUE_PROPERTY ).getValue();
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
                        if ( tree.getItem( o ) != null && tree.getItem( o ).getItemProperty( VALUE_PROPERTY ) != null
                                && tree.getItem( o ).getItemProperty( VALUE_PROPERTY ).getValue() != null )
                        {
                            ContainerHost containerHost =
                                    ( ContainerHost ) tree.getItem( o ).getItemProperty( VALUE_PROPERTY ).getValue();
                            selectedList.add( containerHost );
                        }
                    }
                    selectedContainers = selectedList;
                }
            }
        } );
        GridLayout grid = new GridLayout( 1, 3 );
        grid.addComponent( new Label( "Environments:" ), 0, 0 );
        grid.addComponent( envCombo, 0, 1 );
        grid.addComponent( tree, 0, 2 );

        addComponent( grid );
        addDetachListener( new DetachListener()
        {
            @Override
            public void detach( final DetachEvent event )
            {
                scheduler.shutdown();
            }
        } );
        addAttachListener( new AttachListener()
        {
            @Override
            public void attach( final AttachEvent event )
            {
                startTreeUpdate();
            }
        } );
    }


    private void startTreeUpdate()
    {
        scheduler = Executors.newScheduledThreadPool( 1 );

        final Date lastUpdateDate = new Date( System.currentTimeMillis() - 31 * 1000 );

        scheduler.scheduleWithFixedDelay( new Runnable()
        {
            public void run()
            {
                try
                {
                    if ( lastUpdateDate.getTime() < updateDate.getTime()
                            || System.currentTimeMillis() - lastUpdateDate.getTime() > 30 * 1000 )
                    {

                        //refresh envCombo combo
                        Set<Environment> environments = environmentManager.getEnvironments();
                        Set<Environment> envs = Sets.newHashSet(
                                ( Collection<Environment> ) envCombo.getContainerDataSource().getItemIds() );

                        for ( Environment environment1 : environments )
                        {
                            if ( !envs.contains( environment1 ) )
                            {
                                envCombo.getContainerDataSource().addItem( environment1 );
                            }
                        }

                        for ( Environment environment1 : envs )
                        {
                            if ( !environments.contains( environment1 ) )
                            {
                                envCombo.getContainerDataSource().removeItem( environment1 );
                            }
                        }

                        //refresh selected env container tree
                        if ( environment != null )
                        {
                            refreshContainers();
                        }

                        lastUpdateDate.setTime( System.currentTimeMillis() );
                    }
                }
                catch ( Exception e )
                {
                    LOG.error( "Error refreshing environment tree", e );
                }
            }
        }, 0, 1, TimeUnit.SECONDS );
    }


    public HierarchicalContainer getNodeContainer()
    {
        container = new HierarchicalContainer();
        container.addContainerProperty( VALUE_PROPERTY, ContainerHost.class, null );
        container.addContainerProperty( ICON_PROPERTY, Resource.class, new ThemeResource( "img/lxc/physical.png" ) );

        return container;
    }


    private static class MyCustomFilter implements Container.Filter
    {

        private String propertyId;
        private Set<String> containerNames;

        public MyCustomFilter( final String propertyId, final Set<String> containerNames )
        {
            this.propertyId = propertyId;
            this.containerNames = containerNames;
        }


        @Override
        public boolean passesFilter( final Object itemId, final Item item )
        {
            Property p = item.getItemProperty( propertyId );

            // Should always check validity
            if ( p == null || !p.getType().equals( ContainerHost.class ) || p.getValue() == null )
            {
                return false;
            }
            ContainerHost value = ( ContainerHost ) p.getValue();

            // The actual filter logic
            return containerNames.contains( value.getHostname() );
        }


        @Override
        public boolean appliesToProperty( final Object propertyId )
        {
            return propertyId != null && propertyId.equals( this.propertyId );
        }
    }


    public void filterContainerHostsByTag( String tag )
    {

        if ( environment != null )
        {
            container.removeAllContainerFilters();

            if ( !Strings.isNullOrEmpty( tag ) )
            {
                filterContainers( tag );
            }
        }
        else
        {
            Notification.show( "Select environment" );
        }
    }


    private void filterContainers( final String tag )
    {
        try
        {
            Set<String> matchedContainerNames = Sets.newHashSet();

            Environment env = environmentManager.findEnvironment( environment.getId() );

            for ( ContainerHost ec : env.getContainerHosts() )
            {

                if ( ec.getTags().contains( tag ) )
                {
                    matchedContainerNames.add( ec.getHostname() );
                }
            }

            container.addContainerFilter( new MyCustomFilter( VALUE_PROPERTY, matchedContainerNames ) );

            for ( ContainerHost ec : environment.getContainerHosts() )
            {
                if ( !matchedContainerNames.contains( ec.getHostname() ) )
                {
                    String peerId = ec.getPeerId();
                    String itemId = peerId + ":" + ec.getId();
                    tree.unselect( itemId );
                }
            }
        }
        catch ( EnvironmentNotFoundException e )
        {
            LOG.warn( "Error retrieving environment" );
            Notification.show( e.getMessage() );
        }
    }


    public Set<ContainerHost> getSelectedContainers()
    {
        return Collections.unmodifiableSet( selectedContainers );
    }


    private void refreshContainers()
    {

        if ( environment != null )
        {
            try
            {
                Environment env = environmentManager.findEnvironment( environment.getId() );

                Set<String> presentItems = Sets.newHashSet();
                fillWithEnvironmentContainers( env, presentItems );

                Set<String> allItemsIds = Sets.newHashSet();
                allItemsIds.addAll( ( Collection<String> ) container.getItemIds() );
                for ( String itemId : allItemsIds )
                {
                    if ( !presentItems.contains( itemId ) )
                    {
                        container.removeItem( itemId );
                        for ( Iterator<ContainerHost> iterator = selectedContainers.iterator(); iterator.hasNext(); )
                        {
                            final ContainerHost ec = iterator.next();
                            String peerId = ec.getPeerId();
                            String ecId = peerId + ":" + ec.getId();
                            if ( !presentItems.contains( ecId ) )
                            {
                                iterator.remove();
                            }
                        }
                    }
                }
            }
            catch ( EnvironmentNotFoundException e )
            {
                LOG.warn( "Error looking for an environment" );
                environment = null;
                envCombo.setValue( null );
                tree.removeAllItems();
            }
        }
    }


    private void fillWithEnvironmentContainers( final Environment env, final Set<String> presentItems )
    {
        for ( ContainerHost ec : env.getContainerHosts() )
        {
            String peerId = ec.getPeerId();
            String itemId = peerId + ":" + ec.getId();
            presentItems.add( peerId );
            presentItems.add( itemId );

            Item peer = container.getItem( peerId );

            if ( peer == null )
            {
                peer = container.addItem( peerId );
            }
            if ( peer != null )
            {
                container.setChildrenAllowed( peerId, true );

                tree.setItemCaption( peerId, ec.getPeer().getName() );

                peer.getItemProperty( VALUE_PROPERTY ).setValue( null );

                Item item = container.getItem( itemId );

                if ( item == null )
                {
                    item = container.addItem( itemId );
                }

                if ( item != null )
                {
                    container.setParent( itemId, peerId );
                    container.setChildrenAllowed( itemId, false );
                    tree.setItemCaption( itemId, ec.getHostname() );
                    item.getItemProperty( VALUE_PROPERTY ).setValue( ec );

                    item.getItemProperty( ICON_PROPERTY ).setValue( new ThemeResource(
                            ec.isConnected() ? "img/lxc/virtual.png" : "img/lxc/virtual_offline.png" ) );
                }
            }
        }
    }
}
