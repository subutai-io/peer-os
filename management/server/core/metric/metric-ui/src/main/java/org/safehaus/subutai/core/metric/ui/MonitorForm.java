package org.safehaus.subutai.core.metric.ui;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.NamingException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.host.HostInfo;
import org.safehaus.subutai.common.metric.Metric;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.common.util.UnitUtil;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.metric.api.MonitorException;
import org.safehaus.subutai.core.metric.ui.chart.JFreeChartWrapper;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.safehaus.subutai.server.ui.component.HostTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
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
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


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

    private Window showProgress;


    public MonitorForm( ServiceLocator serviceLocator, HostRegistry hostRegistry ) throws NamingException
    {

        setSizeFull();
        monitor = serviceLocator.getService( Monitor.class );
        environmentManager = serviceLocator.getService( EnvironmentManager.class );
        peerManager = serviceLocator.getService( PeerManager.class );

        // Notify user about ongoing progress
        Label icon = new Label();
        icon.setId( "indicator" );
        icon.setIcon( new ThemeResource( "img/spinner.gif" ) );
        icon.setContentMode( ContentMode.HTML );

        HorizontalLayout indicatorLayout = new HorizontalLayout();
        indicatorLayout.addComponent( icon );
        indicatorLayout.setComponentAlignment( icon, Alignment.TOP_LEFT );

        showProgress = new Window( "", indicatorLayout );
        showProgress.setModal( true );
        showProgress.setClosable( false );
        showProgress.setResizable( false );
        showProgress.center();
        showProgress.addFocusListener( new FieldEvents.FocusListener()
        {
            @Override
            public void focus( final FieldEvents.FocusEvent event )
            {
                loadMetrics();
            }
        } );

        HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
        horizontalSplit.setSplitPosition( 200, Unit.PIXELS );

        hostTree = new HostTree( hostRegistry );
        Button getMetricsButton = new Button( "Get Metrics" );
        getMetricsButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                getUI().addWindow( showProgress );
                showProgress.focus();
            }
        } );
        VerticalLayout vLayout = new VerticalLayout( hostTree, getEnvironmentComboBox(), getMetricsButton );
        horizontalSplit.setFirstComponent( vLayout );

        chartsLayout = new VerticalLayout();


        HorizontalLayout controls = new HorizontalLayout();
        controls.setSpacing( true );


        controls.addComponent( getResourceHostsButton() );

        controls.addComponent( new Label( "Environment:" ) );

        //        controls.addComponent( getEnvironmentComboBox() );

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

        //        horizontalSplit.setSecondComponent( content );
        horizontalSplit.setSecondComponent( chartsLayout );

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


    private void showHostsMetrics( Collection<? extends Metric> chartsData )
    {
        chartsLayout.removeAllComponents();
        addCpuMetrics( chartsData );
        addRamMetrics( chartsData );
        addDiskMetricsPack( chartsData );
        //        addHomeDiskMetrics( chartsData );
        //        addOptDiskMetrics( chartsData );
        //        addVarDiskMetrics( chartsData );
        //        addRootfsDiskMetrics( chartsData );
    }


    private void loadMetrics()
    {
        List<Metric> metricList = new ArrayList<>();
        Set<ContainerHost> localContainerHosts = new HashSet<>();
        for ( final HostInfo hostInfo : hostTree.getSelectedHosts() )
        {
            try
            {
                if ( hostTree.getNodeContainer().getParent( hostInfo.getId() ) != null )
                {

                    ContainerHost containerHost = peerManager.getLocalPeer().getContainerHostById( hostInfo.getId() );
                    localContainerHosts.add( containerHost );
                }
                else
                {
                    ResourceHost resourceHost = peerManager.getLocalPeer().getResourceHostById( hostInfo.getId() );
                    metricList.add( monitor.getResourceHostMetric( resourceHost ) );
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
        if ( localContainerHosts.size() > 0 )
        {
            metricList.addAll( monitor.getLocalContainerHostsMetrics( localContainerHosts ) );
        }

        Environment environment = ( Environment ) environmentCombo.getValue();
        if ( environment != null )
        {
            try
            {
                metricList.addAll( monitor.getContainerHostsMetrics( environment ) );
            }
            catch ( MonitorException e )
            {
                LOG.error( "Error getting environment container hosts metrics", e );
            }
        }

        showHostsMetrics( metricList );
        environmentCombo.setValue( null );
        Object[] selectedItems = ( ( Set<Object> ) hostTree.getTree().getValue() ).toArray();
        for ( final Object selectedItem : selectedItems )
        {
            hostTree.getTree().unselect( selectedItem );
        }
        showProgress.close();
    }


    private void addCpuMetrics( Collection<? extends Metric> hostMetrics )
    {
        Map<String, Double> cpuHostMetrics = new HashMap<>();
        for ( final Metric hostsMetric : hostMetrics )
        {
            cpuHostMetrics.put( hostsMetric.getHost(), hostsMetric.getUsedCpu() );
        }
        addMetrics( cpuHostMetrics, "CPU", "CPU Metric", "Host(s)", "Metrics scale" );
    }


    private void addRamMetrics( Collection<? extends Metric> hostMetrics )
    {
        Map<String, Double> ramHostMetrics = new HashMap<>();
        for ( final Metric hostsMetric : hostMetrics )
        {
            ramHostMetrics.put( hostsMetric.getHost(), hostsMetric.getUsedRam() );
        }
        addMetrics( ramHostMetrics, "RAM", "RAM Metric", "Host(s)", "Metrics scale" );
    }


    private void addHomeDiskMetrics( Collection<? extends Metric> hostMetrics )
    {
        Map<String, Double> metrics = new HashMap<>();
        for ( final Metric hostsMetric : hostMetrics )
        {
            metrics.put( hostsMetric.getHost(), hostsMetric.getUsedDiskHome() );
        }
        addMetrics( metrics, "Home Folder", "Home disk Metric", "Metric Scale", "Metrics" );
    }


    private void addVarDiskMetrics( Collection<? extends Metric> hostMetrics )
    {
        Map<String, Double> metrics = new HashMap<>();
        for ( final Metric hostsMetric : hostMetrics )
        {
            metrics.put( hostsMetric.getHost(), hostsMetric.getUsedDiskVar() );
        }
        addMetrics( metrics, "Var folder", "Var disk metric", "Metric Scale", "Metrics" );
    }


    private void addOptDiskMetrics( Collection<? extends Metric> hostMetrics )
    {
        Map<String, Double> metrics = new HashMap<>();
        for ( final Metric hostsMetric : hostMetrics )
        {
            metrics.put( hostsMetric.getHost(), hostsMetric.getUsedDiskOpt() );
        }
        addMetrics( metrics, "Opt folder", "Opt disk metric", "Metric Scale", "Metrics" );
    }


    private void addRootfsDiskMetrics( Collection<? extends Metric> hostMetrics )
    {
        Map<String, Double> metrics = new HashMap<>();
        for ( final Metric hostsMetric : hostMetrics )
        {
            metrics.put( hostsMetric.getHost(), hostsMetric.getUsedDiskVar() );
        }
        addMetrics( metrics, "Rootfs folder", "Rootfs disk metric", "Metric Scale", "Metrics" );
    }


    private void addMetrics( Map<String, Double> values, String rowKey, String chartTitle, String categoryAxisLabel,
                             String valueAxisLabel )
    {
        try
        {
            /* Step - 1: Define the data for the line chart  */
            DefaultCategoryDataset line_chart_dataset = new DefaultCategoryDataset();
            for ( final Map.Entry<String, Double> entry : values.entrySet() )
            {
                line_chart_dataset.addValue( entry.getValue(), rowKey,
                        entry.getKey() + "\n" + String.valueOf( entry.getValue() ) );
            }
            /* Step -2:Define the JFreeChart object to create line chart */
            JFreeChart lineChartObject = ChartFactory
                    .createBarChart( chartTitle, categoryAxisLabel, valueAxisLabel, line_chart_dataset,
                            PlotOrientation.VERTICAL, true, true, false );
            JFreeChartWrapper jFreeChartWrapper = new JFreeChartWrapper( lineChartObject );
            chartsLayout.addComponent( jFreeChartWrapper );
        }
        catch ( Exception e )
        {
            Notification.show( e.getMessage() );
        }
    }


    private void addDiskMetricsPack( Collection<? extends Metric> hostMetrics )
    {
        try
        {
            /* Step - 1: Define the data for the line chart  */
            DefaultCategoryDataset line_chart_dataset = new DefaultCategoryDataset();

            Map<String, Double> rootfsMetrics = new HashMap<>();
            for ( final Metric hostsMetric : hostMetrics )
            {
                rootfsMetrics.put( hostsMetric.getHost(), hostsMetric.getUsedDiskVar() );
            }
            for ( final Map.Entry<String, Double> entry : rootfsMetrics.entrySet() )
            {
                line_chart_dataset.addValue( entry.getValue(), "rootfs", entry.getKey() );
            }

            Map<String, Double> optMetrics = new HashMap<>();
            for ( final Metric hostsMetric : hostMetrics )
            {
                optMetrics.put( hostsMetric.getHost(), hostsMetric.getUsedDiskOpt() );
            }
            for ( final Map.Entry<String, Double> entry : optMetrics.entrySet() )
            {
                line_chart_dataset.addValue( entry.getValue(), "opt", entry.getKey() );
            }

            Map<String, Double> varMetrics = new HashMap<>();
            for ( final Metric hostsMetric : hostMetrics )
            {
                varMetrics.put( hostsMetric.getHost(), hostsMetric.getUsedDiskVar() );
            }
            for ( final Map.Entry<String, Double> entry : varMetrics.entrySet() )
            {
                line_chart_dataset.addValue( entry.getValue(), "var", entry.getKey() );
            }


            Map<String, Double> homeMetrics = new HashMap<>();
            for ( final Metric hostsMetric : hostMetrics )
            {
                homeMetrics.put( hostsMetric.getHost(), hostsMetric.getUsedDiskHome() );
            }
            for ( final Map.Entry<String, Double> entry : homeMetrics.entrySet() )
            {
                line_chart_dataset.addValue( entry.getValue(), "home", entry.getKey() );
            }


            /* Step -2:Define the JFreeChart object to create line chart */
            JFreeChart lineChartObject = ChartFactory
                    .createBarChart( "Disk Metrics", "Host(s) directories", "Disk Scale", line_chart_dataset,
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
