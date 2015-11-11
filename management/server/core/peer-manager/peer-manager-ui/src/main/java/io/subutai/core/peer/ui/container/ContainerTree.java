package io.subutai.core.peer.ui.container;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

import io.subutai.common.host.Interface;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.metric.HostMetric;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.settings.Common;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.metric.api.Monitor;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ManagementHost;
import io.subutai.server.ui.component.ConcurrentComponent;


public class ContainerTree extends ConcurrentComponent implements HostListener
{
    private static final String VALUE_PROPERTY = "value";
    private static final String ICON_PROPERTY = "icon";

    private static final Logger LOG = LoggerFactory.getLogger( ContainerTree.class.getName() );
    private final LocalPeer localPeer;
    private final Tree tree;
    private HierarchicalContainer container;
    private Set<Host> selectedHosts = new HashSet<>();
    private Item managementHostItem;
    private Monitor monitor;


    public ContainerTree( LocalPeer localPeer, final HostRegistry hostRegistry, final Monitor monitor )
    {

        this.localPeer = localPeer;
        this.monitor = monitor;
        setSizeFull();
        setMargin( true );

        tree = new Tree( "List of nodes" );
        initView( hostRegistry );
    }


    private void initView( final HostRegistry hostRegistry )
    {
        container = new HierarchicalContainer();
        container.addContainerProperty( VALUE_PROPERTY, Host.class, null );
        container.addContainerProperty( ICON_PROPERTY, Resource.class, new ThemeResource( "img/lxc/physical.png" ) );

        tree.setContainerDataSource( getNodeContainer() );
        tree.setItemIconPropertyId( ICON_PROPERTY );
        tree.setItemDescriptionGenerator( new AbstractSelect.ItemDescriptionGenerator()
        {

            @Override
            public String generateDescription( Component source, Object itemId, Object propertyId )
            {
                Item item = tree.getItem( itemId );
                StringBuilder result = new StringBuilder();
                if ( item != null )
                {
                    Host host = ( Host ) item.getItemProperty( VALUE_PROPERTY ).getValue();
                    if ( host != null )
                    {
                        try
                        {
                            String intfName = "br-int";
                            if ( host instanceof ContainerHost )
                            {
                                intfName = Common.DEFAULT_CONTAINER_INTERFACE;
                            }
                            Interface intf = host.getInterfaceByName( intfName );
                            String mac = null;
                            String ip = null;
                            if ( intf != null )
                            {
                                mac = intf.getMac();
                                ip = intf.getIp();
                            }

                            result.append( getText( "Hostname: %s", host.getHostname() ) );
                            result.append( getText( "<br/>ID: %s", host.getId() ) );
                            result.append( getText( "<br/>IP: %s", ip ) );
                            result.append( getText( "<br/>MAC: %s", mac ) );


                            if ( host instanceof ResourceHost )
                            {

                                ResourceHost resourceHost = ( ResourceHost ) host;
                                final HostMetric metric = monitor.getHostMetric( resourceHost.getId() );

                                result.append( getText( "<br>ARCH: %s", resourceHost.getArch() ) );
                                result.append( getText( "<br>CPU model: %s", metric.getCpuModel() ) );
                                result.append( getText( "<br>CPU core(s): %d", metric.getCpuCore() ) );
                                result.append( getText( "<br>CPU load: %.2f", metric.getUsedCpu() ) );
                                result.append( getText( "<br>Total RAM: %.3f Gb",
                                        metric.getTotalRam() / 1024 / 1024 / 1024 ) );
                                result.append( getText( "<br>Available RAM: %.3f Gb",
                                        metric.getAvailableRam() / 1024 / 1024 / 1024 ) );
                                result.append( getText( "<br>Available space: %.3f Gb",
                                        metric.getAvailableSpace() / 1024 / 1024 / 1024 ) );
                            }
                        }
                        catch ( Exception ignore )
                        {
                            // ignore
                        }
                    }
                }

                return result.toString();
            }
        } );
        tree.setMultiSelect( false );
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
                        if ( tree.getItem( o ).getItemProperty( VALUE_PROPERTY ).getValue() != null )
                        {
                            Host host = ( Host ) tree.getItem( o ).getItemProperty( VALUE_PROPERTY ).getValue();
                            selectedList.add( host );
                        }
                    }
                    selectedHosts = selectedList;
                }
            }
        } );

        //added this to insert static CSS ids to tree items since we can not set HTML element ids for them
        tree.setItemStyleGenerator( new Tree.ItemStyleGenerator()
        {

            public String getStyle( Tree tree, Object itemId )
            {

                Item item = tree.getItem( itemId );
                if ( item != null )
                {
                    Host host = ( Host ) item.getItemProperty( VALUE_PROPERTY ).getValue();
                    if ( host != null )
                    {
                        return "hostname_" + host.getHostname();
                    }
                }

                return null;
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

        for ( ResourceHost rh : localPeer.getResourceHosts() )
        {
            tree.expandItem( rh.getId() );
        }
    }


    private String getText( final String format, final Object o )
    {
        if ( o != null )
        {
            return String.format( format, o );
        }
        else
        {
            return "";
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
                    managementHostItem.getItemProperty( VALUE_PROPERTY ).setValue( managementHost );
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
                String itemId = ( String ) itemObj;
                Item item = container.getItem( itemId );
                Object o = item.getItemProperty( VALUE_PROPERTY ).getValue();
                if ( ( o instanceof Host ) && ( ( ( Host ) o ).isConnected() ) )
                {
                    item.getItemProperty( ICON_PROPERTY ).setValue( new ThemeResource( "img/lxc/virtual.png" ) );
                }
                else
                {
                    item.getItemProperty( ICON_PROPERTY )
                        .setValue( new ThemeResource( "img/lxc/virtual_offline.png" ) );
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
        resourceHostItem.getItemProperty( VALUE_PROPERTY ).setValue( rh );
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
                containerHostItem.getItemProperty( VALUE_PROPERTY ).setValue( ch );
                container.setParent( ch.getId(), rh.getId() );
            }
            tree.expandItem( rh.getId() );
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
                Object containerHost = item.getItemProperty( VALUE_PROPERTY ).getValue();
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
