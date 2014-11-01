package org.safehaus.subutai.core.metric.ui;


import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.metric.api.MonitorException;
import org.safehaus.subutai.core.metric.api.ResourceHostMetric;

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

    private Monitor monitor;
    private EnvironmentManager environmentManager;
    private TextArea outputTxtArea;
    private ComboBox environmentCombo;


    public MonitorForm( ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
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


    private Component getContainerHostsButton()
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
                    addOutput( environment.toString() );
                }
            }
        } );

        return button;
    }


    private Component getResourceHostsButton()
    {
        Button button = new Button( "Get Resource Hosts Metrics" );

        button.setId( "btnResourceHostsMetrics" );
        button.setStyleName( "default" );
        button.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
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
                    e.printStackTrace();
                }
            }
        } );

        return button;
    }


    private Component getOutputArea()
    {
        outputTxtArea = new TextArea( "Metrics" );
        outputTxtArea.setId( "outputTxtArea" );
        outputTxtArea.setImmediate( true );
        outputTxtArea.setWordwrap( true );
        outputTxtArea.setSizeFull();
        outputTxtArea.setRows( 30 );
        return outputTxtArea;
    }


    private Component getEnvironmentComboBox()
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


    private void addOutput( String output )
    {

        if ( !Strings.isNullOrEmpty( output ) )
        {
            outputTxtArea.setValue( String.format( "%s\n%s", outputTxtArea.getValue(), output ) );
            outputTxtArea.setCursorPosition( outputTxtArea.getValue().length() - 1 );
        }
    }
}
