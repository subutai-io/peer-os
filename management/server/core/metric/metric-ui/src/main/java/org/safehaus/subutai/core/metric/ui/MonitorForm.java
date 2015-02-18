package org.safehaus.subutai.core.metric.ui;


import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.NamingException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.metric.Metric;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.common.util.UnitUtil;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.metric.api.MonitorException;
import org.safehaus.subutai.core.metric.ui.chart.JFreeChartWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;


public class MonitorForm extends CustomComponent
{
    private static final Logger LOG = LoggerFactory.getLogger( MonitorPortalModule.class.getName() );

    private Monitor monitor;
    private EnvironmentManager environmentManager;
    private ComboBox environmentCombo;
    protected Table metricTable;
    private Label indicator;
    private Button showRHMetricsBtn;
    private Button showCHMetricsBtn;
    private UnitUtil unitUtil = new UnitUtil();
    protected ExecutorService executorService = Executors.newCachedThreadPool();


    public MonitorForm( ServiceLocator serviceLocator ) throws NamingException
    {

        monitor = serviceLocator.getService( Monitor.class );
        environmentManager = serviceLocator.getService( EnvironmentManager.class );


        final GridLayout content = new GridLayout();
        content.setSpacing( true );
        content.setSizeFull();
        content.setMargin( true );
        content.setRows( 25 );
        content.setColumns( 1 );

        HorizontalLayout controls = new HorizontalLayout();
        controls.setSpacing( true );

        content.addComponent( controls, 0, 0 );

        content.addComponent( getMetricTable(), 0, 1, 0, 9 );

        controls.addComponent( getResourceHostsButton() );

        controls.addComponent( new Label( "Environment:" ) );

        controls.addComponent( getEnvironmentComboBox() );

        controls.addComponent( getContainerHostsButton() );

        controls.addComponent( getIndicator() );

        Button chartBtn = new Button( "Chart" );

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
                    content.addComponent( jFreeChartWrapper );
                }
                catch ( Exception e )
                {
                    Notification.show( e.getMessage() );
                }
            }
        } );

        controls.addComponent( chartBtn );

        setCompositionRoot( content );

        addDetachListener( new DetachListener()
        {
            @Override
            public void detach( final DetachEvent event )
            {
                executorService.shutdown();
            }
        } );
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
