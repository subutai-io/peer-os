/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.zookeeper.ui.wizard;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.ui.ZookeeperUI;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;


/**
 * @author dilshat
 */
public class ConfigurationStep extends Panel {

    public ConfigurationStep( final Wizard wizard ) {

        if ( wizard.getConfig().getSetupType() == SetupType.STANDALONE ) {

            //Standalone cluster installation controls

            GridLayout standaloneInstallationControls = new GridLayout( 1, 5 );
            standaloneInstallationControls.setSizeFull();
            standaloneInstallationControls.setSpacing( true );
            standaloneInstallationControls.setMargin( true );

            final TextField clusterNameTxtFld = new TextField( "Enter cluster name" );
            clusterNameTxtFld.setInputPrompt( "Cluster name" );
            clusterNameTxtFld.setRequired( true );
            clusterNameTxtFld.setMaxLength( 20 );
            clusterNameTxtFld.setValue( wizard.getConfig().getClusterName() );
            clusterNameTxtFld.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    wizard.getConfig().setClusterName( event.getProperty().getValue().toString().trim() );
                }
            } );
            final TextField zkNameTxtFld = new TextField( "Enter zk name" );
            zkNameTxtFld.setInputPrompt( "ZK name" );
            zkNameTxtFld.setRequired( true );
            zkNameTxtFld.setMaxLength( 20 );
            zkNameTxtFld.setValue( wizard.getConfig().getZkName() );
            zkNameTxtFld.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    wizard.getConfig().setZkName( event.getProperty().getValue().toString().trim() );
                }
            } );

            //number of nodes
            ComboBox nodesCountCombo =
                    new ComboBox( "Choose number of nodes", Arrays.asList( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ) );
            nodesCountCombo.setImmediate( true );
            nodesCountCombo.setTextInputAllowed( false );
            nodesCountCombo.setNullSelectionAllowed( false );
            nodesCountCombo.setValue( wizard.getConfig().getNumberOfNodes() );

            nodesCountCombo.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    wizard.getConfig().setNumberOfNodes( ( Integer ) event.getProperty().getValue() );
                }
            } );

            Button next = new Button( "Next" );
            next.addStyleName( "default" );
            next.addClickListener( new Button.ClickListener() {

                @Override
                public void buttonClick( Button.ClickEvent event ) {

                    if ( Strings.isNullOrEmpty( wizard.getConfig().getClusterName() ) ) {
                        show( "Please provide cluster name" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getConfig().getZkName() ) ) {
                        show( "Please provide zk name" );
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
                public void buttonClick( Button.ClickEvent event ) {
                    wizard.back();
                }
            } );


            HorizontalLayout buttons = new HorizontalLayout();
            buttons.addComponent( back );
            buttons.addComponent( next );

            standaloneInstallationControls.addComponent(
                    new Label( "Please, specify installation settings for standalone cluster installation" ) );
            standaloneInstallationControls.addComponent( clusterNameTxtFld );
            standaloneInstallationControls.addComponent( zkNameTxtFld );
            standaloneInstallationControls.addComponent( nodesCountCombo );
            standaloneInstallationControls.addComponent( buttons );

            setContent( standaloneInstallationControls );
        }
        else if ( wizard.getConfig().getSetupType() == SetupType.OVER_HADOOP ) {

            //Over Hadoop cluster installation controls

            GridLayout overHadoopInstallationControls = new GridLayout( 1, 5 );
            overHadoopInstallationControls.setSizeFull();
            overHadoopInstallationControls.setSpacing( true );
            overHadoopInstallationControls.setMargin( true );

            final TextField zkNameTxtFld = new TextField( "Enter zk name" );
            zkNameTxtFld.setInputPrompt( "ZK name" );
            zkNameTxtFld.setRequired( true );
            zkNameTxtFld.setMaxLength( 20 );
            zkNameTxtFld.setValue( wizard.getConfig().getZkName() );
            zkNameTxtFld.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    wizard.getConfig().setZkName( event.getProperty().getValue().toString().trim() );
                }
            } );

            ComboBox hadoopClustersCombo = new ComboBox( "Hadoop cluster" );
            final TwinColSelect hadoopNodesSelect = new TwinColSelect( "Nodes", new ArrayList<Agent>() );

            hadoopClustersCombo.setImmediate( true );
            hadoopClustersCombo.setTextInputAllowed( false );
            hadoopClustersCombo.setRequired( true );
            hadoopClustersCombo.setNullSelectionAllowed( false );

            List<HadoopClusterConfig> clusters = ZookeeperUI.getHadoopManager().getClusters();
            if ( clusters.size() > 0 ) {
                for ( HadoopClusterConfig hadoopClusterInfo : clusters ) {
                    hadoopClustersCombo.addItem( hadoopClusterInfo );
                    hadoopClustersCombo.setItemCaption( hadoopClusterInfo, hadoopClusterInfo.getClusterName() );
                }
            }

            HadoopClusterConfig info = ZookeeperUI.getHadoopManager().getCluster( wizard.getConfig().getClusterName() );

            if ( info != null ) {
                hadoopClustersCombo.setValue( info );
            }
            else if ( clusters.size() > 0 ) {
                hadoopClustersCombo.setValue( clusters.iterator().next() );
            }

            if ( hadoopClustersCombo.getValue() != null ) {
                HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) hadoopClustersCombo.getValue();
                wizard.getConfig().setClusterName( hadoopInfo.getClusterName() );
                hadoopNodesSelect
                        .setContainerDataSource( new BeanItemContainer<>( Agent.class, hadoopInfo.getAllNodes() ) );
            }

            hadoopClustersCombo.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    if ( event.getProperty().getValue() != null ) {
                        HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) event.getProperty().getValue();
                        hadoopNodesSelect.setValue( null );
                        hadoopNodesSelect.setContainerDataSource(
                                new BeanItemContainer<>( Agent.class, hadoopInfo.getAllNodes() ) );
                        wizard.getConfig().setClusterName( hadoopInfo.getClusterName() );
                        wizard.getConfig().setNodes( new HashSet<Agent>() );
                    }
                }
            } );

            hadoopNodesSelect.setItemCaptionPropertyId( "hostname" );
            hadoopNodesSelect.setRows( 7 );
            hadoopNodesSelect.setMultiSelect( true );
            hadoopNodesSelect.setImmediate( true );
            hadoopNodesSelect.setLeftColumnCaption( "Available Nodes" );
            hadoopNodesSelect.setRightColumnCaption( "Selected Nodes" );
            hadoopNodesSelect.setWidth( 100, Unit.PERCENTAGE );
            hadoopNodesSelect.setRequired( true );

            if ( !Util.isCollectionEmpty( wizard.getConfig().getNodes() ) ) {
                hadoopNodesSelect.setValue( wizard.getConfig().getNodes() );
            }
            hadoopNodesSelect.addValueChangeListener( new Property.ValueChangeListener() {

                public void valueChange( Property.ValueChangeEvent event ) {
                    if ( event.getProperty().getValue() != null ) {
                        Set<Agent> agentList = new HashSet<>( ( Collection<Agent> ) event.getProperty().getValue() );
                        wizard.getConfig().setNodes( agentList );
                    }
                }
            } );

            Button next = new Button( "Next" );
            next.addStyleName( "default" );
            next.addClickListener( new Button.ClickListener() {

                @Override
                public void buttonClick( Button.ClickEvent event ) {
                    if ( Strings.isNullOrEmpty( wizard.getConfig().getClusterName() ) ) {
                        show( "Please, select Hadoop cluster" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getConfig().getZkName() ) ) {
                        show( "Please provide zk name" );
                    }
                    else if ( Util.isCollectionEmpty( wizard.getConfig().getNodes() ) ) {
                        show( "Please, select target nodes" );
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
                public void buttonClick( Button.ClickEvent event ) {
                    wizard.back();
                }
            } );

            HorizontalLayout buttons = new HorizontalLayout();
            buttons.addComponent( back );
            buttons.addComponent( next );

            overHadoopInstallationControls.addComponent(
                    new Label( "Please, specify installation settings for over-Hadoop cluster installation" ) );
            overHadoopInstallationControls.addComponent( zkNameTxtFld );
            overHadoopInstallationControls.addComponent( hadoopClustersCombo );
            overHadoopInstallationControls.addComponent( hadoopNodesSelect );
            overHadoopInstallationControls.addComponent( buttons );
            setContent( overHadoopInstallationControls );
        }
    }


    private void show( String notification ) {
        Notification.show( notification );
    }
}
