package org.safehaus.subutai.server.ui.component;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.hostregistry.api.HostListener;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;
import org.safehaus.subutai.core.peer.api.Host;
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
    private Set<Agent> currentAgents = new HashSet<>();
    private Set<Host> selectedHosts = Sets.newHashSet();


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
                    Host host = ( Host ) item.getItemProperty( "value" ).getValue();
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

        hostRegistry.addHostListener( this );
    }


    public HierarchicalContainer getNodeContainer()
    {
        container = new HierarchicalContainer();
        container.addContainerProperty( "value", Host.class, null );
        container.addContainerProperty( "icon", Resource.class, new ThemeResource( "img/lxc/physical.png" ) );
        //        refreshAgents( agentManager.getAgents() );
        return container;
    }


    private void refreshAgents( Set<Agent> allFreshAgents )
    {
        if ( allFreshAgents != null )
        {
            try
            {

                currentAgents.removeAll( allFreshAgents );

                if ( !currentAgents.isEmpty() )
                {
                    for ( Agent missingAgent : currentAgents )
                    {
                        container.removeItemRecursively( missingAgent.getUuid() );
                    }
                }

                //grab parents
                Set<Agent> parents = new HashSet<>();
                for ( Agent agent : allFreshAgents )
                {
                    if ( !agent.isLXC() && agent.getUuid() != null && agent.getHostname() != null )
                    {
                        parents.add( agent );
                    }
                }

                //find children
                Set<Agent> possibleOrphans = new HashSet<>();
                Map<Agent, Set<Agent>> families = new HashMap<>();
                if ( !parents.isEmpty() )
                {
                    Set<Agent> childAgentsWithParents = new HashSet<>();
                    for ( Agent parent : parents )
                    {
                        //find children
                        Set<Agent> children = new HashSet<>();
                        for ( Agent possibleChild : allFreshAgents )
                        {
                            if ( possibleChild.isLXC() && possibleChild.getUuid() != null
                                    && possibleChild.getHostname() != null )
                            {
                                //add for further orphan children processing
                                possibleOrphans.add( possibleChild );
                                //check if this is own child
                                if ( parent.getHostname().equalsIgnoreCase( possibleChild.getParentHostName() ) )
                                {
                                    children.add( possibleChild );
                                }
                            }
                        }
                        if ( !children.isEmpty() )
                        {
                            //add children to parent
                            childAgentsWithParents.addAll( children );
                            families.put( parent, children );
                        }
                        else
                        {
                            families.put( parent, null );
                        }
                    }

                    //remove all child agents having parents
                    possibleOrphans.removeAll( childAgentsWithParents );
                }
                else
                {
                    //all agents are orphans
                    for ( Agent possibleChild : allFreshAgents )
                    {
                        if ( possibleChild.isLXC() && possibleChild.getUuid() != null
                                && possibleChild.getHostname() != null )
                        {
                            //add for further orphan children processing
                            possibleOrphans.add( possibleChild );
                        }
                    }
                }

                //add families to tree
                if ( !families.isEmpty() )
                {
                    for ( Map.Entry<Agent, Set<Agent>> family : families.entrySet() )
                    {
                        Agent parentAgent = family.getKey();

                        Item parent = container.getItem( parentAgent.getUuid() );
                        //agent is not yet in the tree
                        if ( parent == null )
                        {
                            parent = container.addItem( parentAgent.getUuid() );
                        }
                        if ( parent != null )
                        {
                            tree.setItemCaption( parentAgent.getUuid(), parentAgent.getHostname() );
                            parent.getItemProperty( "value" ).setValue( parentAgent );
                            if ( family.getValue() != null )
                            {
                                container.setChildrenAllowed( parentAgent.getUuid(), true );
                                for ( Agent childAgent : family.getValue() )
                                {
                                    Item child = container.getItem( childAgent.getUuid() );
                                    //child is not yet in the tree
                                    if ( child == null )
                                    {
                                        child = container.addItem( childAgent.getUuid() );
                                    }
                                    if ( child != null )
                                    {
                                        tree.setItemCaption( childAgent.getUuid(), childAgent.getHostname() );
                                        child.getItemProperty( "value" ).setValue( childAgent );
                                        child.getItemProperty( "icon" )
                                             .setValue( new ThemeResource( "img/lxc/virtual.png" ) );
                                        container.setParent( childAgent.getUuid(), parentAgent.getUuid() );
                                        container.setChildrenAllowed( childAgent.getUuid(), false );
                                    }
                                }
                            }
                            else
                            {
                                container.setChildrenAllowed( parentAgent.getUuid(), false );
                            }
                        }
                    }
                }

                //add orphans to tree
                if ( !possibleOrphans.isEmpty() )
                {
                    Item parent = container.getItem( Common.UNKNOWN_LXC_PARENT_NAME );
                    if ( parent == null )
                    {
                        parent = container.addItem( Common.UNKNOWN_LXC_PARENT_NAME );
                    }
                    if ( parent != null )
                    {
                        container.setChildrenAllowed( Common.UNKNOWN_LXC_PARENT_NAME, true );
                        for ( Agent orphanAgent : possibleOrphans )
                        {
                            Item child = container.getItem( orphanAgent.getUuid() );
                            //orphan is not yet in the tree
                            if ( child == null )
                            {
                                child = container.addItem( orphanAgent.getUuid() );
                            }
                            if ( child != null )
                            {
                                tree.setItemCaption( orphanAgent.getUuid(), orphanAgent.getHostname() );
                                child.getItemProperty( "value" ).setValue( orphanAgent );
                                child.getItemProperty( "icon" ).setValue( new ThemeResource( "img/lxc/virtual.png" ) );
                                container.setParent( orphanAgent.getUuid(), Common.UNKNOWN_LXC_PARENT_NAME );
                                container.setChildrenAllowed( orphanAgent.getUuid(), false );
                            }
                        }
                    }
                }
                else
                {
                    container.removeItemRecursively( Common.UNKNOWN_LXC_PARENT_NAME );
                }

                currentAgents = allFreshAgents;
                container.sort( new Object[] { "value" }, new boolean[] { true } );
            }
            catch ( Property.ReadOnlyException | Converter.ConversionException ex )
            {
                LOG.error( "Error in refreshAgents", ex );
            }
        }
    }


    public Set<Host> getSelectedHosts()
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

    }
}
