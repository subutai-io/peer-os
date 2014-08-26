/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mongodb.ui.wizard;


import java.util.Arrays;

import org.safehaus.subutai.common.StringUtil;

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


/**
 * @author dilshat
 */
public class ConfigurationStep extends VerticalLayout {

    public ConfigurationStep( final Wizard wizard ) {

        setSizeFull();

        GridLayout content = new GridLayout( 2, 7 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );

        final TextField clusterNameTxtFld = new TextField( "Enter cluster name" );
        clusterNameTxtFld.setInputPrompt( "Cluster name" );
        clusterNameTxtFld.setRequired( true );
        clusterNameTxtFld.setMaxLength( 20 );
        clusterNameTxtFld.setValue( wizard.getMongoClusterConfig().getClusterName() );
        clusterNameTxtFld.addValueChangeListener( new Property.ValueChangeListener() {
            @Override
            public void valueChange( Property.ValueChangeEvent event ) {
                wizard.getMongoClusterConfig().setClusterName( event.getProperty().getValue().toString().trim() );
            }
        } );

        //configuration servers number
        ComboBox cfgSrvsCombo =
                new ComboBox( "Choose number of configuration servers (Recommended 3 nodes)", Arrays.asList( 1, 3 ) );
        cfgSrvsCombo.setImmediate( true );
        cfgSrvsCombo.setTextInputAllowed( false );
        cfgSrvsCombo.setNullSelectionAllowed( false );
        cfgSrvsCombo.setValue( wizard.getMongoClusterConfig().getNumberOfConfigServers() );

        cfgSrvsCombo.addValueChangeListener( new Property.ValueChangeListener() {
            @Override
            public void valueChange( Property.ValueChangeEvent event ) {
                wizard.getMongoClusterConfig().setNumberOfConfigServers( ( Integer ) event.getProperty().getValue() );
            }
        } );

        //routers number
        ComboBox routersCombo =
                new ComboBox( "Choose number of routers ( At least 2 recommended)", Arrays.asList( 1, 2, 3 ) );
        routersCombo.setImmediate( true );
        routersCombo.setTextInputAllowed( false );
        routersCombo.setNullSelectionAllowed( false );
        routersCombo.setValue( wizard.getMongoClusterConfig().getNumberOfRouters() );

        routersCombo.addValueChangeListener( new Property.ValueChangeListener() {
            @Override
            public void valueChange( Property.ValueChangeEvent event ) {
                wizard.getMongoClusterConfig().setNumberOfRouters( ( Integer ) event.getProperty().getValue() );
            }
        } );

        //datanodes number
        ComboBox dataNodesCombo = new ComboBox( "Choose number of datanodes", Arrays.asList( 3, 5, 7 ) );
        dataNodesCombo.setImmediate( true );
        dataNodesCombo.setTextInputAllowed( false );
        dataNodesCombo.setNullSelectionAllowed( false );
        dataNodesCombo.setValue( wizard.getMongoClusterConfig().getNumberOfDataNodes() );

        dataNodesCombo.addValueChangeListener( new Property.ValueChangeListener() {
            @Override
            public void valueChange( Property.ValueChangeEvent event ) {
                wizard.getMongoClusterConfig().setNumberOfDataNodes( ( Integer ) event.getProperty().getValue() );
            }
        } );

        TextField replicaSetName = new TextField( "Enter replica set name" );
        replicaSetName.setInputPrompt( wizard.getMongoClusterConfig().getReplicaSetName() );
        replicaSetName.setMaxLength( 20 );
        replicaSetName.addValueChangeListener( new Property.ValueChangeListener() {
            @Override
            public void valueChange( Property.ValueChangeEvent event ) {
                String value = event.getProperty().getValue().toString().trim();
                if ( !Strings.isNullOrEmpty( value ) ) {
                    wizard.getMongoClusterConfig().setReplicaSetName( value );
                }
            }
        } );

        TextField cfgSrvPort = new TextField( "Enter port for configuration servers" );
        cfgSrvPort.setInputPrompt( wizard.getMongoClusterConfig().getCfgSrvPort() + "" );
        cfgSrvPort.setMaxLength( 5 );
        cfgSrvPort.addValueChangeListener( new Property.ValueChangeListener() {
            @Override
            public void valueChange( Property.ValueChangeEvent event ) {
                String value = event.getProperty().getValue().toString().trim();
                if ( StringUtil.isNumeric( value ) ) {
                    wizard.getMongoClusterConfig().setCfgSrvPort( Integer.parseInt( value ) );
                }
            }
        } );

        TextField routerPort = new TextField( "Enter port for routers" );
        routerPort.setInputPrompt( wizard.getMongoClusterConfig().getRouterPort() + "" );
        routerPort.setMaxLength( 5 );
        routerPort.addValueChangeListener( new Property.ValueChangeListener() {
            @Override
            public void valueChange( Property.ValueChangeEvent event ) {
                String value = event.getProperty().getValue().toString().trim();
                if ( StringUtil.isNumeric( value ) ) {
                    wizard.getMongoClusterConfig().setRouterPort( Integer.parseInt( value ) );
                }
            }
        } );

        TextField dataNodePort = new TextField( "Enter port for data nodes" );
        dataNodePort.setInputPrompt( wizard.getMongoClusterConfig().getDataNodePort() + "" );
        dataNodePort.setMaxLength( 5 );
        dataNodePort.addValueChangeListener( new Property.ValueChangeListener() {
            @Override
            public void valueChange( Property.ValueChangeEvent event ) {
                String value = event.getProperty().getValue().toString().trim();
                if ( StringUtil.isNumeric( value ) ) {
                    wizard.getMongoClusterConfig().setDataNodePort( Integer.parseInt( value ) );
                }
            }
        } );

        TextField domain = new TextField( "Enter domain name" );
        domain.setInputPrompt( wizard.getMongoClusterConfig().getDomainName() );
        domain.setMaxLength( 20 );
        domain.addValueChangeListener( new Property.ValueChangeListener() {
            @Override
            public void valueChange( Property.ValueChangeEvent event ) {
                String value = event.getProperty().getValue().toString().trim();
                if ( !Strings.isNullOrEmpty( value ) ) {
                    wizard.getMongoClusterConfig().setDomainName( value );
                }
            }
        } );

        Button next = new Button( "Next" );
        next.addStyleName( "default" );
        next.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                if ( Strings.isNullOrEmpty( wizard.getMongoClusterConfig().getClusterName() ) ) {
                    show( "Please provide cluster name" );
                }
                else {
                    wizard.next();
                }
            }
        } );

        Button back = new Button( "Back" );
        back.addStyleName( "default" );
        back.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
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
        content.addComponent( cfgSrvsCombo );
        content.addComponent( replicaSetName );
        content.addComponent( routersCombo );
        content.addComponent( domain );
        content.addComponent( dataNodesCombo );
        content.addComponent( cfgSrvPort );
        content.addComponent( new Label() );
        content.addComponent( routerPort );
        content.addComponent( new Label() );
        content.addComponent( dataNodePort );
        content.addComponent( new Label() );
        content.addComponent( buttons );

        addComponent( layout );
    }


    private void show( String notification ) {
        Notification.show( notification );
    }
}
