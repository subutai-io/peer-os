/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.hadoop.wizard;


import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.ui.*;

import java.util.ArrayList;
import java.util.List;


/**
 * @author dilshat
 */
public class ConfigurationStep extends VerticalLayout
{

    public ConfigurationStep( final Wizard wizard )
    {

        setSizeFull();

        GridLayout content = new GridLayout( 2, 7 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );

        final TextField clusterNameTxtFld = new TextField( "Enter cluster name" );
        clusterNameTxtFld.setInputPrompt( "Cluster name" );
        clusterNameTxtFld.setRequired( true );
        clusterNameTxtFld.setMaxLength( 20 );
        if ( !Strings.isNullOrEmpty( wizard.getConfig().getClusterName() ) )
        {
            clusterNameTxtFld.setValue( wizard.getConfig().getClusterName() );
        }
        clusterNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                wizard.getConfig().setClusterName( event.getProperty().getValue().toString().trim() );
            }
        } );

        //configuration servers number
        List<Integer> s = new ArrayList<Integer>();
        for ( int i = 1; i < 16; i++ )
        {
            s.add( i );
        }

        ComboBox slaveNodesComboBox = new ComboBox( "Choose number of slave nodes", s );
        slaveNodesComboBox.setImmediate( true );
        slaveNodesComboBox.setTextInputAllowed( false );
        slaveNodesComboBox.setNullSelectionAllowed( false );
        slaveNodesComboBox.setValue( wizard.getConfig().getCountOfSlaveNodes() );

        slaveNodesComboBox.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                wizard.getConfig().setCountOfSlaveNodes( ( Integer ) event.getProperty().getValue() );
            }
        } );

        //configuration replication factor
        List<Integer> s2 = new ArrayList<Integer>();
        for ( int i = 1; i < 6; i++ )
        {
            s2.add( i );
        }

        ComboBox replicationFactorComboBox = new ComboBox( "Choose replication factor for slave nodes", s2 );
        replicationFactorComboBox.setImmediate( true );
        replicationFactorComboBox.setTextInputAllowed( false );
        replicationFactorComboBox.setNullSelectionAllowed( false );
        replicationFactorComboBox.setValue( wizard.getConfig().getReplicationFactor() );

        replicationFactorComboBox.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                wizard.getConfig().setReplicationFactor( ( Integer ) event.getProperty().getValue() );
            }
        } );

        TextField domain = new TextField( "Enter domain name" );
        domain.setInputPrompt( wizard.getConfig().getDomainName() );
        domain.setValue( wizard.getConfig().getDomainName() );
        domain.setMaxLength( 20 );
        domain.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                String value = event.getProperty().getValue().toString().trim();
                if ( !Strings.isNullOrEmpty( value ) )
                {
                    wizard.getConfig().setDomainName( value );
                }
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
                    show( "Please provide cluster name" );
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
        content.addComponent( domain );
        content.addComponent( slaveNodesComboBox );
        content.addComponent( replicationFactorComboBox );
        content.addComponent( buttons );

        addComponent( layout );

    }


    private void show( String notification )
    {
        Notification.show( notification );
    }

}
