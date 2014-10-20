package org.safehaus.subutai.plugin.jetty.ui.wizard;


import java.util.Arrays;

import org.h2.util.StringUtils;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


public class ConfigurationStep extends VerticalLayout
{

    public ConfigurationStep( final Wizard wizard )
    {

        setSizeFull();

        GridLayout content = new GridLayout( 1, 3 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );

        final TextField clusterNameTxtFld = new TextField( "Enter cluster name" );
        clusterNameTxtFld.setInputPrompt( "Cluster name" );
        clusterNameTxtFld.setRequired( true );
        clusterNameTxtFld.setValue( wizard.getConfig().getClusterName() );
        clusterNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                wizard.getConfig().setClusterName( event.getProperty().getValue().toString().trim() );
            }
        } );

        final TextField domainNameTxtFld = new TextField( "Enter domain name" );
        domainNameTxtFld.setInputPrompt( "intra.lan" );
        domainNameTxtFld.setRequired( true );
        domainNameTxtFld.setValue( wizard.getConfig().getClusterName() );
        domainNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                wizard.getConfig().setDomainName( event.getProperty().getValue().toString().trim() );
            }
        } );

        final TextField baseDirectoryText = new TextField( "Enter base directory" );
        baseDirectoryText.setInputPrompt( wizard.getConfig().getBaseDirectory() );
        baseDirectoryText.setRequired( true );
        baseDirectoryText.setValue( wizard.getConfig().getClusterName() );
        baseDirectoryText.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                wizard.getConfig().setBaseDirectory( event.getProperty().getValue().toString().trim() );
            }
        } );

        final TextField portTextField = new TextField( "Enter port" );
        portTextField.setInputPrompt( ""+wizard.getConfig().getPort() );
        portTextField.setRequired( true );
        portTextField.setValue( wizard.getConfig().getClusterName() );
        portTextField.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                String value = event.getProperty().getValue().toString();
                if( !StringUtils.isNumber(value)) {
                    show("Please input numbers");
                    return;
                }
                int port = Integer.valueOf( value );
                wizard.getConfig().setPort( port );
            }
        } );



        final ComboBox nodesCountCombo =
                new ComboBox( "Choose number of nodes in cluster", Arrays.asList( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ) );
        nodesCountCombo.setImmediate( true );
        nodesCountCombo.setImmediate( true );
        nodesCountCombo.setNullSelectionAllowed( false );
        nodesCountCombo.setValue( wizard.getConfig() );

        nodesCountCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                wizard.getConfig().setNumberOfNodes( ( Integer ) event.getProperty().getValue() );
            }
        } );


        Button next = new Button( "Next" );
        next.addStyleName( "default" );
        next.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( Strings.isNullOrEmpty( wizard.getConfig().getClusterName() ) )
                {
                    show( "Please provide cluster name !" );
                }
                else if ( Strings.isNullOrEmpty( wizard.getConfig().getDomainName() ) )
                {
                    show( "Please provide domain name !" );
                }
                else
                {
                    wizard.next();
                }
            }
        } );

        Button back = new Button( "Back" );
        back.addStyleName( "default" );
        back.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                wizard.back();
            }
        } );

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing( true );
        layout.addComponent( new Label( "Please, specify installation settings" ) );
        layout.addComponent( content );

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent( back );
        buttons.addComponent( next );

        content.addComponent( clusterNameTxtFld );
        content.addComponent( domainNameTxtFld );
        content.addComponent( baseDirectoryText );
        content.addComponent( portTextField );
        content.addComponent( nodesCountCombo );
        content.addComponent( buttons );

        addComponent( layout );
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }
}
