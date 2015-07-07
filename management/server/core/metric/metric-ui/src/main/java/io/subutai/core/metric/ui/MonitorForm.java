package io.subutai.core.metric.ui;


import java.awt.BasicStroke;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.naming.NamingException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import io.subutai.common.environment.Environment;
import io.subutai.common.host.HostInfo;
import io.subutai.common.metric.HistoricalMetric;
import io.subutai.common.metric.MetricType;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.env.api.EnvironmentManager;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.ui.chart.JFreeChartWrapper;
import io.subutai.core.peer.api.HostNotFoundException;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.peer.api.ResourceHost;
import io.subutai.server.ui.component.HostTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents;
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
    private static final Logger LOG = LoggerFactory.getLogger( MonitorForm.class );

    private HostTree hostTree;
    private Monitor monitor;
    private EnvironmentManager environmentManager;
    private PeerManager peerManager;
    private ComboBox environmentCombo;
    protected Table metricTable;
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
        indicatorLayout.setComponentAlignment( icon, Alignment.TOP_CENTER );

        showProgress = new Window( "Loading metrics", indicatorLayout );
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

        hostTree = new HostTree( hostRegistry, true );
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


        final GridLayout content = new GridLayout();
        content.setSpacing( true );
        content.setSizeFull();
        content.setMargin( true );
        content.setRows( 25 );
        content.setColumns( 1 );

        content.addComponent( chartsLayout, 0, 10 );

        horizontalSplit.setSecondComponent( chartsLayout );

        horizontalSplit.setSizeFull();
        setCompositionRoot( horizontalSplit );
    }


    private void loadMetrics()
    {
        Map<UUID, List<HistoricalMetric>> historicalCpuMetric = new HashMap<>();
        Map<UUID, List<HistoricalMetric>> historicalRamMetric = new HashMap<>();
        Map<UUID, List<HistoricalMetric>> historicalDiskVarMetric = new HashMap<>();
        Map<UUID, List<HistoricalMetric>> historicalDiskHomeMetric = new HashMap<>();
        Map<UUID, List<HistoricalMetric>> historicalDiskOptMetric = new HashMap<>();
        Map<UUID, List<HistoricalMetric>> historicalDiskRootfsMetric = new HashMap<>();

        Set<Host> hosts = new HashSet<>();
        for ( final HostInfo hostInfo : hostTree.getSelectedHosts() )
        {
            if ( hostTree.getNodeContainer().getParent( hostInfo.getId() ) != null )
            {
                try
                {
                    ContainerHost containerHost = peerManager.getLocalPeer().getContainerHostById( hostInfo.getId() );
                    hosts.add( containerHost );
                }
                catch ( HostNotFoundException e )
                {
                    LOG.error( "Error getting container host by id " + hostInfo.getId().toString(), e );
                }
            }
            else
            {
                try
                {
                    ResourceHost resourceHost = peerManager.getLocalPeer().getResourceHostById( hostInfo.getId() );
                    hosts.add( resourceHost );
                }
                catch ( HostNotFoundException e )
                {
                    LOG.info( "Error getting resource host by id " + hostInfo.getId().toString(), e );
                }
            }
        }


        Environment environment = ( Environment ) environmentCombo.getValue();
        if ( environment != null )
        {
            hosts.addAll( environment.getContainerHosts() );
        }

        if ( hosts.size() > 0 )
        {
            try
            {
                historicalCpuMetric.putAll( monitor.getHistoricalMetrics( hosts, MetricType.CPU ) );
                historicalRamMetric.putAll( monitor.getHistoricalMetrics( hosts, MetricType.RAM ) );
                historicalDiskVarMetric.putAll( monitor.getHistoricalMetrics( hosts, MetricType.DISK_VAR ) );
                historicalDiskHomeMetric.putAll( monitor.getHistoricalMetrics( hosts, MetricType.DISK_HOME ) );
                historicalDiskOptMetric.putAll( monitor.getHistoricalMetrics( hosts, MetricType.DISK_OPT ) );
                historicalDiskRootfsMetric.putAll( monitor.getHistoricalMetrics( hosts, MetricType.DISK_ROOTFS ) );
            }
            catch ( Exception e )
            {
                Notification.show( "Error occurred while getting metrics!", Notification.Type.WARNING_MESSAGE );
                showProgress.close();
                LOG.error( e.getMessage() );
                return;
            }
        }
        else
        {
            Notification.show( "Select host to draw metrics", Notification.Type.WARNING_MESSAGE );
            showProgress.close();
            return;
        }

        chartsLayout.removeAllComponents();
        addCpuMetrics( historicalCpuMetric );
        addRamMetrics( historicalRamMetric );
        addHomeDiskMetrics( historicalDiskHomeMetric );
        addOptDiskMetrics( historicalDiskOptMetric );
        addVarDiskMetrics( historicalDiskVarMetric );
        addRootfsDiskMetrics( historicalDiskRootfsMetric );
        environmentCombo.setValue( null );
        Object[] selectedItems = ( ( Set<Object> ) hostTree.getTree().getValue() ).toArray();
        for ( final Object selectedItem : selectedItems )
        {
            hostTree.getTree().unselect( selectedItem );
        }
        showProgress.close();
    }


    private void addCpuMetrics( Map<UUID, List<HistoricalMetric>> hostMetrics )
    {
        addMetrics( hostMetrics, "CPU(seconds)" );
    }


    private void addRamMetrics( Map<UUID, List<HistoricalMetric>> hostMetrics )
    {
        addMetrics( hostMetrics, "RAM(MB)" );
    }


    private void addHomeDiskMetrics( Map<UUID, List<HistoricalMetric>> hostMetrics )
    {
        addMetrics( hostMetrics, "Home Dataset(MB)" );
    }


    private void addVarDiskMetrics( Map<UUID, List<HistoricalMetric>> hostMetrics )
    {
        addMetrics( hostMetrics, "Var Dataset(MB)" );
    }


    private void addOptDiskMetrics( Map<UUID, List<HistoricalMetric>> hostMetrics )
    {
        addMetrics( hostMetrics, "Opt Dataset(MB)" );
    }


    private void addRootfsDiskMetrics( Map<UUID, List<HistoricalMetric>> hostMetrics )
    {
        addMetrics( hostMetrics, "Rootfs Dataset(MB)" );
    }


    private void addMetrics( Map<UUID, List<HistoricalMetric>> hostMetrics, String chartTitle )
    {
        String categoryXAxis = "Time";
        String categoryYAxis = "Usage";
        try
        {
            XYDataset dataset = createMetricsDataset( hostMetrics );
            JFreeChart lineChartObject = createChart( chartTitle, categoryXAxis, categoryYAxis, dataset );
            JFreeChartWrapper jFreeChartWrapper = new JFreeChartWrapper( lineChartObject );
            chartsLayout.addComponent( jFreeChartWrapper );
        }
        catch ( Exception e )
        {
            Notification.show( e.getMessage() );
        }
    }


    private XYDataset createMetricsDataset( final Map<UUID, List<HistoricalMetric>> hostMetrics )
    {

        TimeSeries localTimeSeries;
        TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();
        if ( hostMetrics.size() == 0 )
        {
            return timeSeriesCollection;
        }

        for ( final Map.Entry<UUID, List<HistoricalMetric>> entry : hostMetrics.entrySet() )
        {
            List<HistoricalMetric> historicalMetrics = entry.getValue();
            localTimeSeries = new TimeSeries( historicalMetrics.get( 0 ).getHost().getHostname() );
            for ( HistoricalMetric historicalMetric : historicalMetrics )
            {
                localTimeSeries.add( new Minute( historicalMetric.getTimestamp() ), historicalMetric.getValue() );
            }
            timeSeriesCollection.addSeries( localTimeSeries );
        }
        return timeSeriesCollection;
    }


    private static JFreeChart createChart( String chartTitle, String categoryXAxis, String categoryYAxis,
                                           XYDataset localXYDataset )
    {
        JFreeChart localJFreeChart = ChartFactory
                .createTimeSeriesChart( chartTitle, categoryXAxis, categoryYAxis, localXYDataset, true, true, false );
        XYPlot localXYPlot = ( XYPlot ) localJFreeChart.getPlot();
        BasicStroke stroke = new BasicStroke( 2.0f );
        XYItemRenderer renderer = localXYPlot.getRenderer();
        int seriesCount = localXYPlot.getSeriesCount();
        for ( int i = 0; i < seriesCount; i++ )
        {
            renderer.setSeriesStroke( i, stroke );
        }
        return localJFreeChart;
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
