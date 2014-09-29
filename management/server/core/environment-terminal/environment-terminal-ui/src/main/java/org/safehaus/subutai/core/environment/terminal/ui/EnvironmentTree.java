package org.safehaus.subutai.core.environment.terminal.ui;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.agent.api.AgentListener;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.environment.api.EnvironmentContainer;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.server.ui.component.ConcurrentComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;


/**
 * @author tjamakeev
 */
@SuppressWarnings("serial")

public final class EnvironmentTree extends ConcurrentComponent implements AgentListener, Disposable
{

    private static final Logger LOG = LoggerFactory.getLogger( UI.getCurrent().getClass().getName() );
    private final AgentManager agentManager;
    private final EnvironmentManager environmentManager;
    private final Tree tree;
    private HierarchicalContainer container;
    private Set<Agent> currentAgents = new HashSet<>();
    private EnvironmentContainer selectedContainer;
    private Environment environment;
    private String peerId;


    public EnvironmentTree( AgentManager agentManager, EnvironmentManager environmentManager )
    {
        this.agentManager = agentManager;

        this.environmentManager = environmentManager;

        if ( environmentManager.getEnvironments() != null && environmentManager.getEnvironments().size() > 0 )
        {
            this.environment = environmentManager.getEnvironments().get( 1 );
        }

        setSizeFull();
        setMargin( true );

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
                    EnvironmentContainer ec = ( EnvironmentContainer ) item.getItemProperty( "value" ).getValue();
                    if ( ec != null )
                    {
                        description = "Hostname: " + ec.getHostname() + "<br>" + "Peer ID: " + ec.getPeerId() + "<br>"
                                + "Agent ID: " + ec.getAgentId() + "<br>" + "Description: " + ec.getDescription();
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

                    List<EnvironmentContainer> selectedList = new ArrayList<EnvironmentContainer>();

                    for ( Object o : ( Iterable<?> ) t.getValue() )
                    {
                        if ( tree.getItem( o ).getItemProperty( "value" ).getValue() != null )
                        {
                            EnvironmentContainer environmentContainer =
                                    ( EnvironmentContainer ) tree.getItem( o ).getItemProperty( "value" ).getValue();
                            selectedList.add( environmentContainer );
                        }
                    }

                    selectedContainer = selectedList.get( 0 );
                }
            }
        } );
        addComponent( tree );

        agentManager.addListener( this );
    }


    public HierarchicalContainer getNodeContainer()
    {
        container = new HierarchicalContainer();
        container.addContainerProperty( "value", EnvironmentContainer.class, null );
        container.addContainerProperty( "icon", Resource.class, new ThemeResource( "img/lxc/physical.png" ) );

        tree.removeAllItems();
        for ( EnvironmentContainer ec : environment.getContainers() )
        {
            String itemId = ec.getPeerId() + ":" + ec.getAgentId();
            Item item = container.addItem( itemId );
            container.setChildrenAllowed( itemId, false );
            tree.setItemCaption( item, ec.getHostname() );
            item.getItemProperty( "value" ).setValue( ec );
        }
        //        refreshAgents( agentManager.getAgents() );

        return container;
    }


    private void refreshAgentsOld( Set<Agent> allFreshAgents )
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


    public EnvironmentContainer getSelectedContainer()
    {
        return selectedContainer;
    }


    @Override
    public void onAgent( final Set<Agent> freshAgents )
    {
        executeUpdate( new Runnable()
        {
            @Override
            public void run()
            {
                refreshLocalPeerContainers( freshAgents );
            }
        } );
    }


    private void refreshLocalPeerContainers( final Set<Agent> freshAgents )
    {

        if ( freshAgents == null || freshAgents.size() < 1 )
        {
            return;
        }

        List<String> agentIdList = new ArrayList<>();
        for ( Agent agent : freshAgents )
        {
            if ( peerId == null )
            {
                peerId = agent.getSiteId().toString();
            }
            agentIdList.add( agent.getUuid().toString() );
        }

        for ( Object itemObj : container.getItemIds() )
        {
            String itemId = ( String ) itemObj;
            String[] itemIds = itemId.split( ":" );
            if ( itemIds[0].equals( peerId ) )
            {
                if ( agentIdList.contains( itemIds[1] ) )
                {
                    Item item = container.getItem( itemId );
                    item.getItemProperty( "icon" ).setValue( new ThemeResource( "img/lxc/virtual.png" ) );
                }
                else
                {
                    Item item = container.getItem( itemId );
                    item.getItemProperty( "icon" ).setValue( new ThemeResource( "img/lxc/virtual-stopped.png" ) );
                }
            }
        }
    }


    public void dispose()
    {
        agentManager.removeListener( this );
    }
}
