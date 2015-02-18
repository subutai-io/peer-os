package org.safehaus.subutai.server.ui.component;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;

import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.host.HostInfo;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostListener;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;


/**
 * This component allows to see currently connected "local" hosts in tree hierarchy
 */
@SuppressWarnings( "serial" )

public class HostTree extends ConcurrentComponent implements HostListener, Disposable
{

    private static final Logger LOG = LoggerFactory.getLogger( HostTree.class.getName() );
    private final HostRegistry hostRegistry;
    private final Tree tree;
    private HierarchicalContainer container;
    private Set<HostInfo> presentHosts = Sets.newHashSet();
    private Set<HostInfo> selectedHosts = Sets.newHashSet();
    private static final String VALUE_PROPERTY = "value";
    private static final String ICON_PROPERTY = "icon";


    public HostTree( HostRegistry hostRegistry )
    {
        this.hostRegistry = hostRegistry;

        setSizeFull();
        setMargin( true );

        tree = new Tree( "Hosts" );
        tree.setContainerDataSource( getNodeContainer() );
        tree.setItemIconPropertyId( ICON_PROPERTY );
        tree.setItemDescriptionGenerator( new AbstractSelect.ItemDescriptionGenerator()
        {

            @Override
            public String generateDescription( Component source, Object itemId, Object propertyId )
            {
                String description = "";

                Item item = tree.getItem( itemId );
                if ( item != null )
                {
                    HostInfo host = ( HostInfo ) item.getItemProperty( VALUE_PROPERTY ).getValue();
                    if ( host != null )
                    {
                        description = "Hostname: " + host.getHostname() + "<br>" + "ID: " + host.getId();
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

                    Set<HostInfo> selectedList = new HashSet<>();

                    for ( Object o : ( Iterable<?> ) t.getValue() )
                    {
                        if ( tree.getItem( o ) != null && tree.getItem( o ).getItemProperty( VALUE_PROPERTY ) != null
                                && tree.getItem( o ).getItemProperty( VALUE_PROPERTY ).getValue() != null )
                        {
                            HostInfo host = ( HostInfo ) tree.getItem( o ).getItemProperty( VALUE_PROPERTY ).getValue();
                            selectedList.add( host );
                        }
                    }

                    selectedHosts = selectedList;
                }
            }
        } );
        addComponent( tree );

        hostRegistry.addHostListener( this );
    }


    public Tree getTree()
    {
        return tree;
    }


    public HierarchicalContainer getNodeContainer()
    {
        container = new HierarchicalContainer();
        container.addContainerProperty( VALUE_PROPERTY, HostInfo.class, null );
        container.addContainerProperty( ICON_PROPERTY, Resource.class, new ThemeResource( "img/lxc/physical.png" ) );
        refreshTree( hostRegistry.getResourceHostsInfo() );
        return container;
    }


    class MyCustomFilter implements Container.Filter
    {

        private String propertyId;
        private Set<String> containerNames;


        public MyCustomFilter( final String propertyId, final Set<String> containerNames )
        {
            this.propertyId = propertyId;
            this.containerNames = containerNames;
        }


        @Override
        public boolean passesFilter( final Object itemId, final Item item ) throws UnsupportedOperationException
        {
            Property p = item.getItemProperty( propertyId );

            // Should always check validity
            if ( p == null || !p.getType().equals( HostInfo.class ) )
            {
                return false;
            }
            HostInfo value = ( HostInfo ) p.getValue();

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
        try
        {
            container.removeAllContainerFilters();

            if ( !Strings.isNullOrEmpty( tag ) )
            {
                PeerManager peerManager = ServiceLocator.getServiceNoCache( PeerManager.class );

                Set<String> matchedContainerNames = Sets.newHashSet();

                for ( HostInfo hostInfo : presentHosts )
                {
                    if ( hostInfo instanceof ContainerHostInfo )
                    {
                        ContainerHost containerHost =
                                peerManager.getLocalPeer().getContainerHostById( hostInfo.getId() );
                        if ( containerHost.getTags().contains( tag ) )
                        {
                            matchedContainerNames.add( containerHost.getHostname() );
                        }
                    }
                }

                container.addContainerFilter( new MyCustomFilter( VALUE_PROPERTY, matchedContainerNames ) );

                for ( HostInfo hostInfo : presentHosts )
                {
                    if ( !matchedContainerNames.contains( hostInfo.getHostname() ) )
                    {
                        tree.unselect( hostInfo.getId() );
                    }
                }
            }
        }
        catch ( HostNotFoundException | NamingException e )
        {
            LOG.error( "Error in filterContainerHostsByTag", e );
        }
    }


    private void refreshTree( Set<ResourceHostInfo> hosts )
    {

        try
        {
            Set<HostInfo> missingHosts = Sets.newHashSet( presentHosts );
            presentHosts.clear();

            for ( ResourceHostInfo resourceHostInfo : hosts )
            {
                missingHosts.remove( resourceHostInfo );
                Item parent = container.getItem( resourceHostInfo.getId() );
                //host is not yet in the tree
                if ( parent == null )
                {
                    parent = container.addItem( resourceHostInfo.getId() );
                }
                if ( parent != null )
                {
                    tree.setItemCaption( resourceHostInfo.getId(), resourceHostInfo.getHostname() );
                    parent.getItemProperty( VALUE_PROPERTY ).setValue( resourceHostInfo );
                    if ( !CollectionUtil.isCollectionEmpty( resourceHostInfo.getContainers() ) )
                    {
                        container.setChildrenAllowed( resourceHostInfo.getId(), true );
                        for ( ContainerHostInfo containerHostInfo : resourceHostInfo.getContainers() )
                        {
                            missingHosts.remove( containerHostInfo );

                            Item child = container.getItem( containerHostInfo.getId() );
                            //child is not yet in the tree
                            if ( child == null )
                            {
                                child = container.addItem( containerHostInfo.getId() );
                            }
                            if ( child != null )
                            {
                                tree.setItemCaption( containerHostInfo.getId(), containerHostInfo.getHostname() );
                                child.getItemProperty( VALUE_PROPERTY ).setValue( containerHostInfo );
                                child.getItemProperty( ICON_PROPERTY ).setValue(
                                        containerHostInfo.getStatus() == ContainerHostState.RUNNING ?
                                        new ThemeResource( "img/lxc/virtual.png" ) :
                                        new ThemeResource( "img/lxc/virtual_offline.png" ) );
                                container.setParent( containerHostInfo.getId(), resourceHostInfo.getId() );
                                container.setChildrenAllowed( containerHostInfo.getId(), false );
                            }

                            presentHosts.add( containerHostInfo );
                        }
                    }
                    else
                    {
                        container.setChildrenAllowed( resourceHostInfo.getId(), false );
                    }
                }
                presentHosts.add( resourceHostInfo );
            }

            //remove missing hosts from tree
            for ( HostInfo host : missingHosts )
            {
                container.removeItemRecursively( host.getId() );
            }

            //sort hosts
            container.sort( new Object[] { VALUE_PROPERTY }, new boolean[] { true } );
        }
        catch ( Property.ReadOnlyException | Converter.ConversionException ex )
        {
            LOG.error( "Error in refreshTree", ex );
        }
    }


    public Set<HostInfo> getSelectedHosts()
    {
        return Collections.unmodifiableSet( selectedHosts );
    }


    public void dispose()
    {
        hostRegistry.removeHostListener( this );
    }


    @Override
    public void onHeartbeat( final ResourceHostInfo resourceHostInfo )
    {
        executeUpdate( new Runnable()
        {
            @Override
            public void run()
            {
                refreshTree( hostRegistry.getResourceHostsInfo() );
            }
        } );
    }
}
