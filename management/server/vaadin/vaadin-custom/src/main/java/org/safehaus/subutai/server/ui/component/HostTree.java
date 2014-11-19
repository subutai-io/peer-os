package org.safehaus.subutai.server.ui.component;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostListener;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
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


    public HostTree( HostRegistry hostRegistry )
    {
        this.hostRegistry = hostRegistry;

        setSizeFull();
        setMargin( true );

        tree = new Tree( "Hosts" );
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
                    HostInfo host = ( HostInfo ) item.getItemProperty( "value" ).getValue();
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
                        if ( tree.getItem( o ).getItemProperty( "value" ).getValue() != null )
                        {
                            HostInfo host = ( HostInfo ) tree.getItem( o ).getItemProperty( "value" ).getValue();
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


    public HierarchicalContainer getNodeContainer()
    {
        container = new HierarchicalContainer();
        container.addContainerProperty( "value", HostInfo.class, null );
        container.addContainerProperty( "icon", Resource.class, new ThemeResource( "img/lxc/physical.png" ) );
        refreshTree( hostRegistry.getResourceHostsInfo() );
        return container;
    }


    private void refreshTree( Set<ResourceHostInfo> hosts )
    {
        if ( !CollectionUtil.isCollectionEmpty( hosts ) )
        {
            try
            {
                presentHosts.removeAll( hosts );

                if ( !presentHosts.isEmpty() )
                {
                    for ( HostInfo host : presentHosts )
                    {
                        container.removeItemRecursively( host.getId() );
                    }
                }

                presentHosts.clear();

                for ( ResourceHostInfo resourceHostInfo : hosts )
                {

                    presentHosts.add( resourceHostInfo );
                    Item parent = container.getItem( resourceHostInfo.getId() );
                    //host is not yet in the tree
                    if ( parent == null )
                    {
                        parent = container.addItem( resourceHostInfo.getId() );
                    }
                    if ( parent != null )
                    {
                        tree.setItemCaption( resourceHostInfo.getId(), resourceHostInfo.getHostname() );
                        parent.getItemProperty( "value" ).setValue( resourceHostInfo );
                        if ( !CollectionUtil.isCollectionEmpty( resourceHostInfo.getContainers() ) )
                        {
                            container.setChildrenAllowed( resourceHostInfo.getId(), true );
                            for ( ContainerHostInfo containerHostInfo : resourceHostInfo.getContainers() )
                            {
                                presentHosts.add( containerHostInfo );

                                Item child = container.getItem( containerHostInfo.getId() );
                                //child is not yet in the tree
                                if ( child == null )
                                {
                                    child = container.addItem( containerHostInfo.getId() );
                                }
                                if ( child != null )
                                {
                                    tree.setItemCaption( containerHostInfo.getId(), containerHostInfo.getHostname() );
                                    child.getItemProperty( "value" ).setValue( containerHostInfo );
                                    child.getItemProperty( "icon" )
                                         .setValue( new ThemeResource( "img/lxc/virtual.png" ) );
                                    container.setParent( containerHostInfo.getId(), resourceHostInfo.getId() );
                                    container.setChildrenAllowed( containerHostInfo.getId(), false );
                                }
                            }
                        }
                        else
                        {
                            container.setChildrenAllowed( resourceHostInfo.getId(), false );
                        }
                    }
                }

                container.sort( new Object[] { "value" }, new boolean[] { true } );
            }
            catch ( Property.ReadOnlyException | Converter.ConversionException ex )
            {
                LOG.error( "Error in refreshTree", ex );
            }
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
        refreshTree( hostRegistry.getResourceHostsInfo() );
    }
}
