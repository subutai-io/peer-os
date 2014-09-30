package org.safehaus.subutai.core.environment.terminal.ui;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.environment.api.EnvironmentContainer;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
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


/**
 * @author tjamakeev
 */
@SuppressWarnings( "serial" )

public final class EnvironmentTree extends ConcurrentComponent implements Disposable
{

    private static final Logger LOG = LoggerFactory.getLogger( UI.getCurrent().getClass().getName() );
    private final AgentManager agentManager;
    private final EnvironmentManager environmentManager;
    private final ComboBox env;
    private final Tree tree;
    private HierarchicalContainer container;
    private Set<Agent> currentAgents = new HashSet<>();
    private EnvironmentContainer selectedContainer;
    private Environment environment;
    private String peerId;
    private final ScheduledExecutorService scheduler;


    public EnvironmentTree( AgentManager agentManager, final EnvironmentManager environmentManager )
    {
        this.agentManager = agentManager;

        this.environmentManager = environmentManager;

        scheduler = Executors.newScheduledThreadPool( 1 );

        scheduler.scheduleWithFixedDelay( new Runnable()
        {
            public void run()
            {
                LOG.info( "Refreshing containers state..." );
                if ( environment != null )
                {
                    Set<EnvironmentContainer> containers = environmentManager.getConnectedContainers( environment );
                    refreshContainers( containers );
                }
                LOG.info( "Refreshing done." );
            }
        }, 5, 30, TimeUnit.SECONDS );
        setSizeFull();
        setMargin( true );

        BeanItemContainer<Environment> environments = new BeanItemContainer<Environment>( Environment.class );
        environments.addAll( environmentManager.getEnvironments() );
        env = new ComboBox( null, environments );
        env.setItemCaptionPropertyId( "name" );
        //        env.setWidth( 200, Unit.PIXELS );
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
        tree.setMultiSelect( false );
        tree.setImmediate( true );
        tree.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                LOG.info( event.getProperty().toString() );
                Item item = container.getItem( event.getProperty().getValue() );
                if ( item != null && item.getItemProperty( "value" ).getValue() instanceof EnvironmentContainer )
                {
                    selectedContainer = ( EnvironmentContainer ) item.getItemProperty( "value" ).getValue();
                }
            }
        } );
        GridLayout grid = new GridLayout( 1, 3 );

        grid.addComponent( new Label( "Environments:" ) );
        grid.addComponent( env );
        grid.addComponent( tree );

        addComponent( grid );

        //        agentManager.addListener( this );
    }


    public HierarchicalContainer getNodeContainer()
    {
        container = new HierarchicalContainer();
        container.addContainerProperty( "value", EnvironmentContainer.class, null );
        container.addContainerProperty( "icon", Resource.class, new ThemeResource( "img/lxc/physical.png" ) );

        tree.removeAllItems();
        if ( environment != null )
        {
            for ( EnvironmentContainer ec : environment.getContainers() )
            {
                //TODO: remove next line when persistent API is JPA
                ec.setEnvironment( environment );
                String itemId = ec.getPeerId() + ":" + ec.getAgentId();
                Item item = container.addItem( itemId );
                container.setChildrenAllowed( itemId, false );

                tree.setItemCaption( item, "---> " + ec.getHostname() );
                item.getItemProperty( "value" ).setValue( ec );
            }
        }
        else
        {
            LOG.info( "Environment is null" );
        }
        //        refreshAgents( agentManager.getAgents() );

        return container;
    }


    public EnvironmentContainer getSelectedContainer()
    {
        return selectedContainer;
    }


    //    @Override
    //    public void onAgent( final Set<Agent> freshAgents )
    //    {
    //        executeUpdate( new Runnable()
    //        {
    //            @Override
    //            public void run()
    //            {
    //                refreshContainers( getRemoteFreshAgents( environment ) );
    //            }
    //        } );
    //    }


    private void refreshContainers( final Set<EnvironmentContainer> freshContainers )
    {

        if ( freshContainers == null || freshContainers.size() < 1 )
        {
            return;
        }

        List<String> agentIdList = new ArrayList<>();
        for ( EnvironmentContainer container : freshContainers )
        {
            if ( peerId == null )
            {
                peerId = container.getPeerId().toString();
            }
            agentIdList.add( container.getAgentId().toString() );
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
        //        agentManager.removeListener( this );
        scheduler.shutdown();
    }
}
