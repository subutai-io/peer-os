package org.safehaus.subutai.core.peer.ui.container;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.Host;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.settings.Common;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.hostregistry.api.ResourceHostInfo;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.server.ui.component.ConcurrentComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;


public class ContainerTree extends ConcurrentComponent implements HostListener
{

    private static final Logger LOG = LoggerFactory.getLogger( ContainerTree.class.getName() );
    private final LocalPeer localPeer;
    private final Tree tree;
    private HierarchicalContainer container;
    private Set<Host> selectedHosts = new HashSet<>();
    private Item managementHostItem;


    public ContainerTree( LocalPeer localPeer, final HostRegistry hostRegistry )
    {

        this.localPeer = localPeer;
        setSizeFull();
        setMargin( true );

        tree = new Tree( "List of nodes" );
        initView( hostRegistry );
    }


    private void initView( final HostRegistry hostRegistry )
    {
        container = new HierarchicalContainer();
        container.addContainerProperty( "value", Host.class, null );
        container.addContainerProperty( "icon", Resource.class, new ThemeResource( "img/lxc/physical.png" ) );

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
                    Host host = ( Host ) item.getItemProperty( "value" ).getValue();
                    if ( host != null )
                    {
                        String intfName = "br-int";
                        if ( host instanceof ContainerHost )
                        {
                            intfName = Common.DEFAULT_CONTAINER_INTERFACE;
                        }
                        description = "Hostname: " + host.getHostname() + "<br>" + "MAC: " + host
                                .getMacByInterfaceName( intfName ) + "<br>" + "UUID: " + host.getHostId() + "<br>"
                                + "IP: " + host.getIpByInterfaceName( intfName );
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
                    Set<Host> selectedList = new HashSet<>();
                    for ( Object o : ( Iterable<?> ) t.getValue() )
                    {
                        if ( tree.getItem( o ).getItemProperty( "value" ).getValue() != null )
                        {
                            Host host = ( Host ) tree.getItem( o ).getItemProperty( "value" ).getValue();
                            selectedList.add( host );
                        }
                    }
                    selectedHosts = selectedList;
                }
            }
        } );
        addComponent( tree );

        final ContainerTree currentTree = this;
        addAttachListener( new AttachListener()
        {
            @Override
            public void attach( final AttachEvent event )
            {
                hostRegistry.addHostListener( currentTree );
            }
        } );
        addDetachListener( new DetachListener()
        {
            @Override
            public void detach( final DetachEvent event )
            {
                hostRegistry.removeHostListener( currentTree );
            }
        } );
        try
        {
            if ( localPeer.getManagementHost() != null )
            {
                tree.expandItem( localPeer.getManagementHost().getId() );
            }
        }
        catch ( PeerException ignore )
        {
            LOG.warn( "Error accessing management host" );
        }
    }


    public HierarchicalContainer getNodeContainer()
    {
        try
        {
            ManagementHost managementHost = localPeer.getManagementHost();
            if ( managementHost != null )
            {
                managementHostItem = container.getItem( managementHost.getId() );
                if ( managementHostItem == null )
                {
                    managementHostItem = container.addItem( managementHost.getId() );
                    container.setChildrenAllowed( managementHost.getId(), true );
                    managementHostItem.getItemProperty( "value" ).setValue( managementHost );
                    tree.setItemCaption( managementHost.getId(),
                            String.format( localPeer.getPeerInfo().getName(), localPeer.getPeerInfo().getId() ) );
                }
            }
            for ( ResourceHost rh : localPeer.getResourceHosts() )
            {
                fillTreeWithItems( rh, managementHost );
                removeDestroyedItemsFromTree( rh );
            }

            for ( Object itemObj : container.getItemIds() )
            {
                UUID itemId = ( UUID ) itemObj;
                Item item = container.getItem( itemId );
                Object o = item.getItemProperty( "value" ).getValue();
                if ( ( o instanceof Host ) && ( ( ( Host ) o ).isConnected() ) )
                {
                    item.getItemProperty( "icon" ).setValue( new ThemeResource( "img/lxc/virtual.png" ) );
                }
                else
                {
                    item.getItemProperty( "icon" ).setValue( new ThemeResource( "img/lxc/virtual_offline.png" ) );
                }
            }
        }


        catch ( Exception e )
        {
            LOG.error( "Error on building container tree.", e );
        }


        return container;
    }


    private void fillTreeWithItems( final ResourceHost rh, final ManagementHost managementHost )
    {
        Item resourceHostItem = container.getItem( rh.getId() );

        if ( resourceHostItem == null )
        {
            resourceHostItem = container.addItem( rh.getId() );
        }
        tree.setItemCaption( rh.getId(), rh.getHostname() );
        resourceHostItem.getItemProperty( "value" ).setValue( rh );
        if ( managementHostItem != null )
        {
            container.setParent( rh.getId(), managementHost.getId() );
        }
        if ( rh.getContainerHosts().size() > 0 )
        {
            container.setChildrenAllowed( rh.getId(), true );
            for ( ContainerHost ch : rh.getContainerHosts() )
            {
                Item containerHostItem = container.getItem( ch.getId() );
                if ( containerHostItem == null )
                {
                    containerHostItem = container.addItem( ch.getId() );
                    container.setChildrenAllowed( ch.getId(), false );
                }
                tree.setItemCaption( ch.getId(), ch.getHostname() );
                containerHostItem.getItemProperty( "value" ).setValue( ch );
                container.setParent( ch.getId(), rh.getId() );
            }
        }
        else
        {
            container.setChildrenAllowed( rh.getId(), false );
        }
    }


    private void removeDestroyedItemsFromTree( final ResourceHost rh )
    {
        // removing destroyed containers
        Collection children = container.getChildren( rh.getId() );
        if ( children != null )
        {
            Set<Object> ids = Sets.newConcurrentHashSet( children );
            for ( final Object id : ids )
            {
                Item item = container.getItem( id );
                ContainerHost containerHost = ( ContainerHost ) item.getItemProperty( "value" ).getValue();
                if ( !rh.getContainerHosts().contains( containerHost ) )
                {
                    container.removeItem( item );
                    tree.removeItem( id );
                }
            }
        }
    }


    public void refreshHosts()
    {
        synchronized ( container )
        {
            getNodeContainer();
        }
    }


    public Set<Host> getSelectedHosts()
    {
        return Collections.unmodifiableSet( selectedHosts );
    }


    @Override
    public void onHeartbeat( final ResourceHostInfo resourceHostInfo )
    {
        refreshHosts();
    }
}
