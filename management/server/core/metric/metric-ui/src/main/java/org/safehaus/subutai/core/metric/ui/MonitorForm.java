package org.safehaus.subutai.core.metric.ui;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.NamingException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.host.HostInfo;
import org.safehaus.subutai.common.metric.Metric;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.common.util.UnitUtil;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.metric.api.MonitorException;
import org.safehaus.subutai.core.metric.ui.chart.JFreeChartWrapper;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.server.ui.component.HostTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;


public class MonitorForm extends CustomComponent
{
    private static final Logger LOG = LoggerFactory.getLogger( MonitorPortalModule.class.getName() );

    private static final String CAPTION_PROPERTY = "name";
    private static final String ICON_PROPERTY = "icon";
    private static final String VALUE_PROPERTY = "value";

    private HostTree hostTree;
    private Monitor monitor;
    private EnvironmentManager environmentManager;
    private PeerManager peerManager;
    private ComboBox environmentCombo;
    protected Table metricTable;
    private Label indicator;
    private Button showRHMetricsBtn;
    private Button showCHMetricsBtn;
    private UnitUtil unitUtil = new UnitUtil();
    protected ExecutorService executorService = Executors.newCachedThreadPool();
    private VerticalLayout chartsLayout;

    //Pay attention these values are initialized in methods, later on will refactor this)
    private Tree envTree;


    public MonitorForm( ServiceLocator serviceLocator, HostRegistry hostRegistry ) throws NamingException
    {

        setSizeFull();
        monitor = serviceLocator.getService( Monitor.class );
        environmentManager = serviceLocator.getService( EnvironmentManager.class );
        peerManager = serviceLocator.getService( PeerManager.class );


        HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
        horizontalSplit.setSplitPosition( 200, Unit.PIXELS );

        hostTree = new HostTree( hostRegistry );
        Button getMetricsButton = new Button( "Get Metrics" );
        getMetricsButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                List<Metric> metricList = new ArrayList<Metric>();
                Set<ContainerHost> localContainerHosts = new HashSet<>();
                for ( final HostInfo hostInfo : hostTree.getSelectedHosts() )
                {
                    try
                    {
                        if ( hostTree.getNodeContainer().getParent( hostInfo.getId() ) != null )
                        {

                            ContainerHost containerHost =
                                    peerManager.getLocalPeer().getContainerHostById( hostInfo.getId() );
                            localContainerHosts.add( containerHost );
                        }
                        else
                        {
                            metricList.add( monitor.getResourceHostMetric(
                                    peerManager.getLocalPeer().getResourceHostById( hostInfo.getId() ) ) );
                        }
                    }
                    catch ( HostNotFoundException e )
                    {
                        LOG.error( "Error getting container host by id", e );
                    }
                    catch ( MonitorException e )
                    {
                        LOG.error( "Error getting resource host metric.", e );
                    }
                }
                try
                {
                    metricList.addAll( monitor.getLocalContainerHostsMetrics( localContainerHosts ) );
                }
                catch ( MonitorException e )
                {
                    LOG.error( "Error getting container hosts metrics", e );
                }
                if ( envTree != null )
                {
                    Object[] selectedItems = ( ( Set<Object> ) envTree.getValue() ).toArray();
                    for ( final Object selectedItem : selectedItems )
                    {
                        Item item = envTree.getItem( selectedItem );
                        if ( envTree.getParent( selectedItem ) != null )
                        {
                            ContainerHost containerHost =
                                    ( ContainerHost ) item.getItemProperty( VALUE_PROPERTY ).getValue();
                            //                            try
                            //                            {
                            //                                metricList.add( monitor.getContainerHostMetrics(
                            // containerHost ) );
                            //                            }
                            //                            catch ( MonitorException e )
                            //                            {
                            //                                LOG.error( "Error getting metric for container host", e );
                            //                            }
                        }
                    }
                }
                showHostsMetrics( metricList );
            }
        } );
        VerticalLayout vLayout = new VerticalLayout( hostTree, getEnvironmentsTree(), getMetricsButton );
        horizontalSplit.setFirstComponent( vLayout );

        chartsLayout = new VerticalLayout();


        HorizontalLayout controls = new HorizontalLayout();
        controls.setSpacing( true );

        //        chartsLayout.addComponent( controls );


        controls.addComponent( getResourceHostsButton() );

        controls.addComponent( new Label( "Environment:" ) );

        controls.addComponent( getEnvironmentComboBox() );

        controls.addComponent( getContainerHostsButton() );

        controls.addComponent( getIndicator() );

        Button chartBtn = new Button( "Chart" );

        controls.addComponent( chartBtn );

        final GridLayout content = new GridLayout();
        content.setSpacing( true );
        content.setSizeFull();
        content.setMargin( true );
        content.setRows( 25 );
        content.setColumns( 1 );
        content.addComponent( controls, 0, 0 );

        content.addComponent( getMetricTable(), 0, 1, 0, 9 );
        content.addComponent( chartsLayout, 0, 10 );

        chartBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                try
                {
                /* Step - 1: Define the data for the line chart  */
                    DefaultCategoryDataset line_chart_dataset = new DefaultCategoryDataset();
                    line_chart_dataset.addValue( 15, "schools", "1970" );
                    line_chart_dataset.addValue( 30, "schools", "1980" );
                    line_chart_dataset.addValue( 60, "schools", "1990" );
                    line_chart_dataset.addValue( 120, "schools", "2000" );
                    line_chart_dataset.addValue( 240, "schools", "2010" );

                /* Step -2:Define the JFreeChart object to create line chart */
                    JFreeChart lineChartObject = ChartFactory
                            .createLineChart( "Schools Vs Years", "Year", "Schools Count", line_chart_dataset,
                                    PlotOrientation.VERTICAL, true, true, false );
                    JFreeChartWrapper jFreeChartWrapper = new JFreeChartWrapper( lineChartObject );
                    //                    content.addComponent( jFreeChartWrapper );
                    content.addComponent( jFreeChartWrapper );
                }
                catch ( Exception e )
                {
                    Notification.show( e.getMessage() );
                }
            }
        } );

        horizontalSplit.setSecondComponent( content );
        //        horizontalSplit.setSecondComponent( chartsLayout );

        horizontalSplit.setSizeFull();
        setCompositionRoot( horizontalSplit );

        addDetachListener( new DetachListener()
        {
            @Override
            public void detach( final DetachEvent event )
            {
                executorService.shutdown();
            }
        } );
    }


    private Tree getEnvironmentsTree()
    {
        try
        {
            envTree = new Tree( "Environment Tree" );
            envTree.setContainerDataSource( createTreeContent( environmentManager.getEnvironments() ) );
            envTree.setImmediate( true );
            envTree.setMultiSelect( true );
            envTree.setItemCaptionMode( AbstractSelect.ItemCaptionMode.PROPERTY );
            envTree.setItemCaptionPropertyId( CAPTION_PROPERTY );
            envTree.setItemIconPropertyId( ICON_PROPERTY );
            envTree.addItemClickListener( new ItemClickEvent.ItemClickListener()
            {
                @Override
                public void itemClick( final ItemClickEvent event )
                {
                    if ( envTree.getParent( event.getItemId()) != null )
                    {
                        new Thread( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                envTree.unselect( event.getItemId() );
                            }
                        } ).start();

                    }
                }
            } );

            envTree.setItemDescriptionGenerator( new AbstractSelect.ItemDescriptionGenerator()
            {

                @Override
                public String generateDescription( Component source, Object itemId, Object propertyId )
                {
                    String description = "";
                    Item item = envTree.getItem( itemId );
                    if ( envTree.getParent( itemId ) != null )
                    {
                        //Be careful with this casting, here I'm sure that my object is ContainerHost type
                        //To find out object type look into Tree initialization.
                        ContainerHost containerHost =
                                ( ContainerHost ) item.getItemProperty( VALUE_PROPERTY ).getValue();
                        description += String.format( "Hostname: %s <br> ID: %s", containerHost.getHostname(),
                                containerHost.getId().toString() );
                    }

                    return description;
                }
            } );
        }
        catch ( PeerException e )
        {
            LOG.error( "Error getting environments/container hosts", e );
        }
        return envTree;
    }


    private HierarchicalContainer createTreeContent( Set<Environment> oTrees ) throws PeerException
    {
        HierarchicalContainer envHostContainer = new HierarchicalContainer();
        envHostContainer.addContainerProperty( CAPTION_PROPERTY, String.class, "" );
        envHostContainer.addContainerProperty( VALUE_PROPERTY, Object.class, "" );
        envHostContainer
                .addContainerProperty( ICON_PROPERTY, Resource.class, new ThemeResource( "img/lxc/physical.png" ) );

        new Object()
        {
            @SuppressWarnings( "unchecked" )
            public void put( Set<Environment> data, HierarchicalContainer container ) throws PeerException
            {
                for ( Environment env : data )
                {
                    UUID envId = env.getId();
                    if ( !container.containsId( envId ) )
                    {
                        container.addItem( envId );
                        container.getItem( envId ).getItemProperty( CAPTION_PROPERTY ).setValue( env.getName() );
                        container.getItem( envId ).getItemProperty( VALUE_PROPERTY ).setValue( env );

                        container.setChildrenAllowed( envId, true );

                        Set<ContainerHost> envHosts = env.getContainerHosts();
                        for ( final ContainerHost envHost : envHosts )
                        {
                            if ( !container.containsId( envHost.getId() ) )
                            {
                                container.addItem( envHost.getId() );
                                container.getItem( envHost.getId() ).getItemProperty( CAPTION_PROPERTY )
                                         .setValue( envHost.getHostname() );
                                container.getItem( envHost.getId() ).getItemProperty( VALUE_PROPERTY )
                                         .setValue( envHost );
                                container.getItem( envHost.getId() ).getItemProperty( ICON_PROPERTY ).setValue(
                                        envHost.getState() == ContainerHostState.RUNNING ?
                                        new ThemeResource( "img/lxc/virtual.png" ) :
                                        new ThemeResource( "img/lxc/virtual_offline.png" ) );
                                container.setChildrenAllowed( envHost.getId(), false );

                                container.setChildrenAllowed( envHost, false );
                                container.setParent( envHost.getId(), env.getId() );
                            }
                        }
                    }
                }
            }
        }.put( oTrees, envHostContainer );

        return envHostContainer;
    }


    private void showHostsMetrics( Collection<? extends Metric> chartsData )
    {
        chartsLayout.removeAllComponents();
        addCpuMetrics( chartsData );
        addRamMetrics( chartsData );
    }


    private void addCpuMetrics( Collection<? extends Metric> hostMetrics )
    {
        Map<String, Double> cpuHostMetrics = new HashMap<>();
        for ( final Metric hostsMetric : hostMetrics )
        {
            cpuHostMetrics.put( hostsMetric.getHost(), hostsMetric.getUsedCpu() );
        }

        try
        {
            /* Step - 1: Define the data for the line chart  */
            DefaultCategoryDataset line_chart_dataset = new DefaultCategoryDataset();
            for ( final Map.Entry<String, Double> entry : cpuHostMetrics.entrySet() )
            {
                line_chart_dataset.addValue( entry.getValue(), "CPU", entry.getKey() );
            }
            /* Step -2:Define the JFreeChart object to create line chart */
            JFreeChart lineChartObject = ChartFactory
                    .createLineChart( "CPU Metric", "Metric Scale", "Metrics", line_chart_dataset,
                            PlotOrientation.VERTICAL, true, true, false );
            JFreeChartWrapper jFreeChartWrapper = new JFreeChartWrapper( lineChartObject );
            chartsLayout.addComponent( jFreeChartWrapper );
        }
        catch ( Exception e )
        {
            Notification.show( e.getMessage() );
        }
    }


    private void addRamMetrics( Collection<? extends Metric> hostMetrics )
    {
        Map<String, Double> ramHostMetrics = new HashMap<>();
        for ( final Metric hostsMetric : hostMetrics )
        {
            ramHostMetrics.put( hostsMetric.getHost(), hostsMetric.getUsedRam() );
        }

        try
        {
            /* Step - 1: Define the data for the line chart  */
            DefaultCategoryDataset line_chart_dataset = new DefaultCategoryDataset();
            for ( final Map.Entry<String, Double> entry : ramHostMetrics.entrySet() )
            {
                line_chart_dataset.addValue( entry.getValue(), "RAM", entry.getKey() );
            }
            /* Step -2:Define the JFreeChart object to create line chart */
            JFreeChart lineChartObject = ChartFactory
                    .createLineChart( "RAM Metric", "Metric Scale", "Metrics", line_chart_dataset,
                            PlotOrientation.VERTICAL, true, true, false );
            JFreeChartWrapper jFreeChartWrapper = new JFreeChartWrapper( lineChartObject );
            chartsLayout.addComponent( jFreeChartWrapper );
        }
        catch ( Exception e )
        {
            Notification.show( e.getMessage() );
        }
    }


    protected Component getContainerHostsButton()
    {
        showCHMetricsBtn = new Button( "Get Container Hosts Metrics" );
        showCHMetricsBtn.setId( "btnContainerHostsMetrics" );
        showCHMetricsBtn.setStyleName( "default" );
        showCHMetricsBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                Environment environment = ( Environment ) environmentCombo.getValue();

                if ( environment == null )
                {
                    Notification.show( "Please, select environment" );
                }
                else
                {
                    printContainerMetrics( environment );
                }
            }
        } );

        return showCHMetricsBtn;
    }


    protected void printContainerMetrics( final Environment environment )
    {
        showProgress();
        executorService.submit( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    displayMetrics( monitor.getContainerHostsMetrics( environment ) );
                }
                catch ( MonitorException e )
                {
                    LOG.error( "Error getting container metrics", e );

                    Notification.show( e.getMessage() );
                }
                finally
                {
                    hideProgress();
                }
            }
        } );
    }


    protected void printResourceHostMetrics()
    {
        showProgress();
        executorService.submit( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    displayMetrics( monitor.getResourceHostsMetrics() );
                }
                catch ( Exception e )
                {
                    LOG.error( "Error getting resource host metrics", e );

                    Notification.show( e.getMessage() );
                }
                finally
                {
                    hideProgress();
                }
            }
        } );
    }


    private void showProgress()
    {
        showRHMetricsBtn.setEnabled( false );
        showCHMetricsBtn.setEnabled( false );
        indicator.setVisible( true );
    }


    private void hideProgress()
    {
        showRHMetricsBtn.setEnabled( true );
        showCHMetricsBtn.setEnabled( true );
        indicator.setVisible( false );
    }


    protected void displayMetrics( Set<? extends Metric> metrics )
    {
        metricTable.removeAllItems();
        for ( Metric metric : metrics )
        {
            metricTable.addItem( new Object[] {
                    metric.getHost(), metric.getUsedCpu(),
                    unitUtil.convert( metric.getUsedRam(), UnitUtil.Unit.B, UnitUtil.Unit.MB ),
                    unitUtil.convert( metric.getTotalRam(), UnitUtil.Unit.B, UnitUtil.Unit.MB ),
                    unitUtil.convert( metric.getUsedDiskVar(), UnitUtil.Unit.B, UnitUtil.Unit.GB ),
                    unitUtil.convert( metric.getUsedDiskOpt(), UnitUtil.Unit.B, UnitUtil.Unit.GB ),
                    unitUtil.convert( metric.getUsedDiskHome(), UnitUtil.Unit.B, UnitUtil.Unit.GB ),
                    unitUtil.convert( metric.getUsedDiskRootfs(), UnitUtil.Unit.B, UnitUtil.Unit.GB ),
                    unitUtil.convert( metric.getTotalDiskVar(), UnitUtil.Unit.B, UnitUtil.Unit.GB ),
                    unitUtil.convert( metric.getTotalDiskOpt(), UnitUtil.Unit.B, UnitUtil.Unit.GB ),
                    unitUtil.convert( metric.getTotalDiskHome(), UnitUtil.Unit.B, UnitUtil.Unit.GB ),
                    unitUtil.convert( metric.getTotalDiskRootfs(), UnitUtil.Unit.B, UnitUtil.Unit.GB )
            }, null );
        }
        metricTable.refreshRowCache();
    }


    protected Label getIndicator()
    {
        indicator = new Label();
        indicator.setId( "indicator" );
        indicator.setIcon( new ThemeResource( "img/spinner.gif" ) );
        indicator.setContentMode( ContentMode.HTML );
        indicator.setHeight( 11, Sizeable.Unit.PIXELS );
        indicator.setWidth( 50, Sizeable.Unit.PIXELS );
        indicator.setVisible( false );

        return indicator;
    }


    protected Component getResourceHostsButton()
    {
        showRHMetricsBtn = new Button( "Get Resource Hosts Metrics" );
        showRHMetricsBtn.setId( "btnResourceHostsMetrics" );
        showRHMetricsBtn.setStyleName( "default" );
        showRHMetricsBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                printResourceHostMetrics();
            }
        } );

        return showRHMetricsBtn;
    }


    protected Table getMetricTable()
    {
        metricTable = new Table( "Metrics" );
        metricTable.addContainerProperty( "Hostname", String.class, null );
        metricTable.addContainerProperty( "Used CPU (nanoseconds)", Double.class, null );
        metricTable.addContainerProperty( "Used RAM (Mb)", Double.class, null );
        metricTable.addContainerProperty( "Total RAM (Mb)", Double.class, null );
        metricTable.addContainerProperty( "Used disk VAR (GB)", Double.class, null );
        metricTable.addContainerProperty( "Used disk OPT (GB)", Double.class, null );
        metricTable.addContainerProperty( "Used disk HOME (GB)", Double.class, null );
        metricTable.addContainerProperty( "Used disk ROOTFS (GB)", Double.class, null );
        metricTable.addContainerProperty( "Total disk VAR (GB)", Double.class, null );
        metricTable.addContainerProperty( "Total disk OPT (GB)", Double.class, null );
        metricTable.addContainerProperty( "Total disk HOME (GB)", Double.class, null );
        metricTable.addContainerProperty( "Total disk ROOTFS (GB)", Double.class, null );
        metricTable.setPageLength( 10 );
        metricTable.setSelectable( false );
        metricTable.setEnabled( true );
        metricTable.setImmediate( true );
        metricTable.setSizeFull();
        return metricTable;
    }


    protected Component getEnvironmentComboBox()
    {

        BeanItemContainer<Environment> environments = new BeanItemContainer<>( Environment.class );
        environments.addAll( environmentManager.getEnvironments() );
        environmentCombo = new ComboBox( null, environments );
        environmentCombo.setItemCaptionPropertyId( "name" );
        environmentCombo.setImmediate( true );
        environmentCombo.setTextInputAllowed( false );
        environmentCombo.setNullSelectionAllowed( false );
        return environmentCombo;
    }
}
