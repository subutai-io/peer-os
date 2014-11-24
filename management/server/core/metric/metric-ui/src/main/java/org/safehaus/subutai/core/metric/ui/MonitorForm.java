package org.safehaus.subutai.core.metric.ui;


import java.util.Set;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.metric.api.MonitorException;
import org.safehaus.subutai.core.metric.api.ResourceHostMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.thirdparty.guava.common.base.Strings;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;


public class MonitorForm extends CustomComponent
{
    private static final Logger LOG = LoggerFactory.getLogger( MonitorPortalModule.class.getName() );

    private Monitor monitor;
    private EnvironmentManager environmentManager;
    protected TextArea outputTxtArea;
    private ComboBox environmentCombo;


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

        content.addComponent( getOutputArea(), 0, 1, 0, 9 );

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
                    addOutput( "Please, select environment" );
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
            Set<ContainerHostMetric> metrics = monitor.getContainerMetrics( environment );
            for ( ContainerHostMetric metric : metrics )
            {
                addOutput( metric.toString() );
            }
        }
        catch ( MonitorException e )
        {
            LOG.error( "Error getting container metrics", e );

            addOutput( e.getMessage() );
        }
    }


    protected void printResourceHostMetrics()
    {
        try
        {
            Set<ResourceHostMetric> metrics = monitor.getResourceHostMetrics();
            for ( ResourceHostMetric metric : metrics )
            {
                addOutput( metric.toString() );
            }
        }
        catch ( MonitorException e )
        {
            LOG.error( "Error getting resource host metrics", e );

            addOutput( e.getMessage() );
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


    protected Component getOutputArea()
    {
        outputTxtArea = new TextArea( "Metrics" );
        outputTxtArea.setId( "outputTxtArea" );
        outputTxtArea.setImmediate( true );
        outputTxtArea.setWordwrap( true );
        outputTxtArea.setSizeFull();
        outputTxtArea.setRows( 30 );
        return outputTxtArea;
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


    protected void addOutput( String output )
    {

        if ( !Strings.isNullOrEmpty( output ) )
        {
            outputTxtArea.setValue( String.format( "%s%n%s", outputTxtArea.getValue(), output ) );
            outputTxtArea.setCursorPosition( outputTxtArea.getValue().length() - 1 );
        }
    }
}
