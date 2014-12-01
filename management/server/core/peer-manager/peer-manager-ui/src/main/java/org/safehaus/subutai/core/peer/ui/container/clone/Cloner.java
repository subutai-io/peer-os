package org.safehaus.subutai.core.peer.ui.container.clone;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.core.peer.ui.container.ContainerTree;
import org.safehaus.subutai.core.peer.ui.container.executor.AgentExecutionEvent;
import org.safehaus.subutai.core.peer.ui.container.executor.AgentExecutionEventType;
import org.safehaus.subutai.core.peer.ui.container.executor.AgentExecutionListener;
import org.safehaus.subutai.core.peer.ui.container.executor.AgentExecutor;
import org.safehaus.subutai.core.peer.ui.container.executor.AgentExecutorImpl;
import org.safehaus.subutai.core.peer.ui.container.executor.CloneCommandFactory;
import org.safehaus.subutai.core.strategy.api.ContainerPlacementStrategy;
import org.safehaus.subutai.core.strategy.api.CriteriaDef;
import org.safehaus.subutai.core.strategy.api.ServerMetric;
import org.safehaus.subutai.core.strategy.api.StrategyException;
import org.safehaus.subutai.core.strategy.api.StrategyManager;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Field;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Slider;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings( "serial" )
public class Cloner extends VerticalLayout implements AgentExecutionListener
{
    private static final Logger LOG = Logger.getLogger( Cloner.class.getName() );

    private final ContainerTree containerTree;
    private final LocalPeer localPeer;
    private final Button cloneBtn;
    private final TextField textFieldLxcName;
    private final Slider slider;
    private final ComboBox strategy;
    private final ComboBox template;
    private final Label indicator;
    private final TreeTable lxcTable;
    private final GridLayout topContent;
    private final HorizontalLayout criteriaLayout;
    private final StrategyManager strategyManager;

    private final String physicalHostLabel = "Physical Host";
    private final String statusLabel = "Status";
    private final String okIconSource = "img/ok.png";
    private final String errorIconSource = "img/cancel.png";
    private final String loadIconSource = "img/spinner.gif";
    private final String hostValidatorRegex =
            "^(?=.{1,255}$)[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,61}[0-9A-Za-z])?(?:\\.[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,"
                    + "61}[0-9A-Za-z])?)*\\.?$";
    AtomicInteger countProcessed = null;
    AtomicInteger errorProcessed = null;
    Map<ContainerPlacementStrategy, BeanItemContainer<CriteriaDef>> criteriaBeansMap = new HashMap<>();
    List<ContainerPlacementStrategy> placementStrategies;


    public Cloner( LocalPeer localPeer, StrategyManager strategyManager, ContainerTree containerTree )
    {
        setSpacing( true );
        setMargin( true );

        this.containerTree = containerTree;
        this.localPeer = localPeer;

        this.strategyManager = strategyManager;

        placementStrategies = strategyManager.getPlacementStrategies();
        for ( ContainerPlacementStrategy st : placementStrategies )
        {
            BeanItemContainer<CriteriaDef> beanItems = new BeanItemContainer<>( CriteriaDef.class );
            beanItems.addAll( st.getCriteriaDef() );
            criteriaBeansMap.put( st, beanItems );
        }

        BeanItemContainer<ContainerPlacementStrategy> container =
                new BeanItemContainer<>( ContainerPlacementStrategy.class );
        container.addAll( placementStrategies );

        textFieldLxcName = new TextField();
        slider = new Slider();
        slider.setMin( 1 );
        slider.setMax( 20 );
        slider.setWidth( 200, Unit.PIXELS );
        slider.setImmediate( true );

        cloneBtn = new Button( "Clone" );
        cloneBtn.addStyleName( "default" );
        cloneBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                try
                {
                    startCloneTask();
                }
                catch ( PeerException e )
                {
                    show( e.toString() );
                }
            }
        } );

        Button clearBtn = new Button( "Clear" );
        clearBtn.addStyleName( "default" );
        clearBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                //clear completed
                for ( Object rowId : lxcTable.getItemIds() )
                {
                    Item row = lxcTable.getItem( rowId );
                    if ( row != null )
                    {
                        Embedded statusIcon = ( Embedded ) ( row.getItemProperty( statusLabel ).getValue() );
                        if ( statusIcon != null && (
                                okIconSource.equals( ( ( ThemeResource ) statusIcon.getSource() ).getResourceId() )
                                        || errorIconSource
                                        .equals( ( ( ThemeResource ) statusIcon.getSource() ).getResourceId() ) ) )
                        {
                            lxcTable.removeItem( rowId );
                        }
                    }
                }
                //clear empty parents
                for ( Object rowId : lxcTable.getItemIds() )
                {
                    Item row = lxcTable.getItem( rowId );
                    if ( row != null && row.getItemProperty( physicalHostLabel ).getValue() != null && (
                            lxcTable.getChildren( rowId ) == null || lxcTable.getChildren( rowId ).isEmpty() ) )
                    {
                        lxcTable.removeItem( rowId );
                    }
                }
            }
        } );

        indicator = new Label();
        indicator.setIcon( new ThemeResource( "img/spinner.gif" ) );
        indicator.setContentMode( ContentMode.HTML );
        indicator.setHeight( 11, Unit.PIXELS );
        indicator.setWidth( 50, Unit.PIXELS );
        indicator.setVisible( false );

        criteriaLayout = new HorizontalLayout();
        criteriaLayout.setVisible( false );

        topContent = new GridLayout( 9, 2 );
        topContent.setSpacing( true );

        template = new ComboBox( null, localPeer.getTemplates() );
        template.setWidth( 200, Unit.PIXELS );
        template.setImmediate( true );
        template.setTextInputAllowed( false );
        template.setNullSelectionAllowed( false );

        strategy = new ComboBox( null, container );
        strategy.setItemCaptionPropertyId( "title" );
        strategy.setWidth( 200, Unit.PIXELS );
        strategy.setImmediate( true );
        strategy.setTextInputAllowed( false );
        strategy.setNullSelectionAllowed( false );
        Property.ValueChangeListener listener = new Property.ValueChangeListener()
        {
            public void valueChange( Property.ValueChangeEvent event )
            {
                criteriaLayout.removeAllComponents();
                ContainerPlacementStrategy st = ( ContainerPlacementStrategy ) event.getProperty().getValue();

                BeanItemContainer<CriteriaDef> criteriaBeans = criteriaBeansMap.get( st );

                final Table criteriaTable = new Table( "Strategy criteria", criteriaBeans );
                criteriaTable.setColumnHeaders( "ID", "Name", "Value" );
                criteriaTable.setPageLength( 5 );

                criteriaTable.setSelectable( false );

                criteriaTable.setImmediate( true );
                criteriaTable.setTableFieldFactory( new TableFieldFactory()
                {
                    @Override
                    public Field<?> createField( Container container, Object itemId, Object propertyId,
                                                 Component component )
                    {

                        if ( propertyId.toString().equals( "value" ) )
                        {
                            return new CheckBox();
                        }
                        return null;
                    }
                } );

                criteriaTable.setEditable( true );

                //                criteriaTable.setContainerDataSource(criteriaBeans);

                criteriaLayout.addComponent( criteriaTable );
                criteriaLayout.setVisible( st.hasCriteria() );
            }
        };

        strategy.addValueChangeListener( listener );

        topContent.addComponent( new Label( "Product name" ) );
        topContent.addComponent( textFieldLxcName );
        topContent.addComponent( new Label( "Template" ) );
        topContent.addComponent( template );
        topContent.addComponent( new Label( "Containers count" ) );
        topContent.addComponent( slider );
        topContent.addComponent( cloneBtn );
        topContent.addComponent( clearBtn );
        topContent.addComponent( indicator );
        topContent.addComponent( new Label( "Clone strategy" ) );
        topContent.addComponent( strategy );
        topContent.setComponentAlignment( indicator, Alignment.MIDDLE_CENTER );
        addComponent( topContent );
        addComponent( criteriaLayout );
        lxcTable = createLxcTable( "Containers list", 500 );
        addComponent( lxcTable );
    }


    private void startCloneTask() throws PeerException
    {
        final String productName = textFieldLxcName.getValue().trim();

        if ( Strings.isNullOrEmpty( productName ) )
        {
            show( "Please specify product name" );
            return;
        }

        if ( !Strings.isNullOrEmpty( productName ) && !productName.matches( hostValidatorRegex ) )
        {
            show( "Please, use only letters, digits, dots and hyphens in product name" );
            return;
        }

        if ( strategy.getValue() == null )
        {
            show( "Please specify placement strategy" );
            return;
        }

        Set<Host> resourceHosts =
                containerTree.getSelectedHosts(); //AgentUtil.filterPhysicalAgents( agentTree.getSelectedAgents() );
        final Map<Host, List<String>> resourceHostFamilies = new HashMap<>();
        final double count = slider.getValue();
        List<CriteriaDef> criteria = new ArrayList<>();
        if ( resourceHosts.isEmpty() )
        { // process cloning by selected strategy

            if ( placementStrategies == null || placementStrategies.isEmpty() )
            {
                show( "There is no placement strategy." );
                return;
            }
            ContainerPlacementStrategy selectedStrategy =
                    placementStrategies.get( placementStrategies.indexOf( strategy.getValue() ) );
            if ( selectedStrategy.hasCriteria() )
            {
                BeanItemContainer<CriteriaDef> beans = criteriaBeansMap.get( selectedStrategy );
                criteria = Lists.newArrayList( beans.getItemIds() );
                for ( CriteriaDef c : criteria )
                {
                    LOG.info( String.format( "%s %s %s", c.getId(), c.getTitle(), c.getValue() ) );
                }
            }

            List<ServerMetric> serverMetrics = new ArrayList<>();

            for ( Host host : localPeer.getResourceHosts() )
            {
                if ( host instanceof ResourceHost )
                {
                    ResourceHost rh = ( ResourceHost ) host;
                    try
                    {
                        serverMetrics.add( rh.getMetric() );
                    }
                    catch ( PeerException e )
                    {
                        show( e.toString() );
                    }
                }
            }
            Map<ServerMetric, Integer> bestServers = null;
            try
            {
                List<Criteria> cl = new ArrayList<>();
                for ( CriteriaDef c : criteria )
                {
                    cl.add( c );
                }
                bestServers = strategyManager
                        .getPlacementDistribution( serverMetrics, ( int ) count, selectedStrategy.getId(), cl );
            }
            catch ( StrategyException e )
            {
                show( e.toString() );
                return;
            }
            Map<ServerMetric, Integer> sortedBestServers = CollectionUtil.sortMapByValueDesc( bestServers );

            Iterator<ServerMetric> serverMetricIterator = sortedBestServers.keySet().iterator();
            while ( serverMetricIterator.hasNext() )
            {
                ServerMetric serverMetric = serverMetricIterator.next();
                ResourceHost rh = localPeer.getResourceHostByName( serverMetric.getHostname() );
                List<String> lxcHostNames = new ArrayList<>();
                for ( int i = 0; i < sortedBestServers.get( serverMetric ); i++ )
                {
                    lxcHostNames.add( String.format( "%s%d%s", productName, lxcHostNames.size() + 1,
                            UUIDUtil.generateTimeBasedUUID().toString().replace( "-", "" ) ).substring( 0, 11 ) );
                }
                resourceHostFamilies.put( rh, lxcHostNames );
            }
            //            for ( int i = 1; i <= count; i++ )
            //            {
            //                final Map.Entry<ServerMetric, Integer> entry = sortedBestServers.entrySet().iterator()
            // .next();
            //                bestServers.put( entry.getKey(), entry.getValue() - 1 );
            //                List<String> lxcHostNames = resourceHostFamilies.get( entry.getKey() );
            //                if ( lxcHostNames == null )
            //                {
            //                    lxcHostNames = new ArrayList<>();
            //                    ResourceHost rh = localPeer.getResourceHostByName( entry.getKey().getHostname() );
            //                    resourceHostFamilies.put( rh, lxcHostNames );
            //                }
            //                lxcHostNames.add( String.format( "%s%d%s", productName, lxcHostNames.size() + 1,
            //                        UUIDUtil.generateTimeBasedUUID().toString().replace( "-", "" ) ).substring( 0,
            // 11 ) );
            //            }
        }
        else
        { // process cloning in selected hosts
            for ( Host physAgent : resourceHosts )
            {
                List<String> lxcHostNames = new ArrayList<>();
                for ( int i = 1; i <= count; i++ )
                {
                    lxcHostNames.add( String.format( "%s%d%s", productName, lxcHostNames.size() + 1,
                            UUIDUtil.generateTimeBasedUUID().toString().replace( "-", "" ) ).substring( 0, 11 ) );
                }
                resourceHostFamilies.put( physAgent, lxcHostNames );
            }
        }

        indicator.setVisible( true );
        populateLxcTable( resourceHostFamilies );
        countProcessed = new AtomicInteger( ( int ) ( count ) );
        errorProcessed = new AtomicInteger( 0 );
        UUID envId = UUIDUtil.generateRandomUUID();
        for ( final Map.Entry<Host, List<String>> host : resourceHostFamilies.entrySet() )
        {
            AgentExecutor agentExecutor = new AgentExecutorImpl( host.getKey().getHostname(), host.getValue() );
            agentExecutor.addListener( this );
            ExecutorService executor = Executors.newFixedThreadPool( 1 );
            agentExecutor.execute( executor, new CloneCommandFactory( localPeer, envId, host.getKey().getHostname(),
                    ( String ) template.getValue() ) );
            executor.shutdown();
        }
    }


    private void show( String msg )
    {
        Notification.show( msg );
    }


    private void populateLxcTable( Map<Host, List<String>> agents )
    {

        for ( final Map.Entry<Host, List<String>> entry : agents.entrySet() )
        {
            Host host = entry.getKey();
            if ( lxcTable.getItem( host.getHostname() ) == null )
            {
                lxcTable.addItem( new Object[] { host.getHostname(), null, null }, host.getHostname() );
            }
            lxcTable.setCollapsed( host.getHostname(), false );
            for ( String lxc : entry.getValue() )
            {
                //                Embedded progressIcon = new Embedded("", new ThemeResource(loadIconSource));

                lxcTable.addItem( new Object[] {
                        null, lxc, null /*progressIcon*/
                }, lxc );

                lxcTable.setParent( lxc, host.getHostname() );
                lxcTable.setChildrenAllowed( lxc, false );
            }
        }
    }


    private TreeTable createLxcTable( String caption, int size )
    {
        TreeTable table = new TreeTable( caption );
        table.addContainerProperty( physicalHostLabel, String.class, null );
        table.addContainerProperty( "Lxc Host", String.class, null );
        table.addContainerProperty( statusLabel, Embedded.class, null );
        table.setWidth( 100, Unit.PERCENTAGE );
        table.setHeight( size, Unit.PIXELS );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );
        return table;
    }


    @Override
    public void onExecutionEvent( AgentExecutionEvent event )
    {
        updateContainerStatus( event );
    }


    private void updateContainerStatus( final AgentExecutionEvent event )
    {
        getUI().access( new Runnable()
        {
            @Override
            public void run()
            {
                Item row = lxcTable.getItem( event.getContainerName() );
                if ( row != null )
                {
                    Property p = row.getItemProperty( "Status" );
                    if ( AgentExecutionEventType.START.equals( event.getEventType() ) )
                    {
                        p.setValue( new Embedded( "", new ThemeResource( loadIconSource ) ) );
                    }
                    else if ( AgentExecutionEventType.SUCCESS.equals( event.getEventType() ) )
                    {
                        p.setValue( new Embedded( "", new ThemeResource( okIconSource ) ) );
                        countProcessed.decrementAndGet();
                    }
                    else if ( AgentExecutionEventType.FAIL.equals( event.getEventType() ) )
                    {
                        p.setValue( new Embedded( "", new ThemeResource( errorIconSource ) ) );
                        countProcessed.decrementAndGet();
                        errorProcessed.incrementAndGet();
                    }
                }

                if ( countProcessed.intValue() == 0 )
                {
                    indicator.setVisible( false );
                    if ( errorProcessed.intValue() == 0 )
                    {
                        show( "Cloning containers finished successfully." );
                    }
                    else
                    {
                        show( "Not all containers successfully created." );
                    }
                }
            }
        } );
    }
}
