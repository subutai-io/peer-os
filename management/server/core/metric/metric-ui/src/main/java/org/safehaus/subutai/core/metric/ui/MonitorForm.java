package org.safehaus.subutai.core.metric.ui;


import java.util.Set;

import javax.naming.NamingException;

import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.metric.Metric;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.common.util.UnitUtil;
import org.safehaus.subutai.core.env.api.EnvironmentManager;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.metric.api.MonitorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.util.BeanItemContainer;
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
    private UnitUtil unitUtil = new UnitUtil();


    public MonitorForm( ServiceLocator serviceLocator ) throws NamingException
    {

        monitor = serviceLocator.getService( Monitor.class );
        environmentManager = serviceLocator.getService( EnvironmentManager.class );


        final GridLayout content = new GridLayout();
        content.setSpacing( true );
        content.setSizeFull();
        content.setMargin( true );
        content.setRows( 10 );
        content.setColumns( 1 );

        HorizontalLayout controls = new HorizontalLayout();
        controls.setSpacing( true );

        content.addComponent( controls, 0, 0 );

        content.addComponent( getMetricTable(), 0, 1, 0, 9 );

        controls.addComponent( getResourceHostsButton() );

        controls.addComponent( new Label( "Environment:" ) );

        controls.addComponent( getEnvironmentComboBox() );

        controls.addComponent( getContainerHostsButton() );

        setCompositionRoot( content );
    }


    protected Component getContainerHostsButton()
    {
        Button button = new Button( "Get Container Hosts Metrics" );

        button.setId( "btnContainerHostsMetrics" );
        button.setStyleName( "default" );
        button.addClickListener( new Button.ClickListener()
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

        return button;
    }


    protected void printContainerMetrics( Environment environment )
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


    protected void printResourceHostMetrics()
    {
        try
        {
            displayMetrics( monitor.getResourceHostsMetrics() );
        }
        catch ( MonitorException e )
        {
            LOG.error( "Error getting resource host metrics", e );

            Notification.show( e.getMessage() );
        }
    }


    protected Component getResourceHostsButton()
    {
        Button button = new Button( "Get Resource Hosts Metrics" );

        button.setId( "btnResourceHostsMetrics" );
        button.setStyleName( "default" );
        button.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                printResourceHostMetrics();
            }
        } );

        return button;
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
