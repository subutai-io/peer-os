/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.accumulo.ui.wizard;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.safehaus.subutai.plugin.accumulo.api.SetupType;
import org.safehaus.subutai.plugin.accumulo.ui.AccumuloUI;
import org.safehaus.subutai.plugin.accumulo.ui.common.UiUtil;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;

import com.google.common.base.Strings;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;


/**
 * @author dilshat
 */
public class ConfigurationStep extends Panel {
    private Property.ValueChangeListener masterNodeComboChangeListener;
    private Property.ValueChangeListener gcNodeComboChangeListener;


    public ConfigurationStep( final Wizard wizard ) {

        List<Integer> nodesCountRange =
                ContiguousSet.create( Range.closed( 1, 50 ), DiscreteDomain.integers() ).asList();

        if ( wizard.getConfig().getSetupType() == SetupType.OVER_HADOOP_N_ZK ) {
            //hadoop combo
            final ComboBox hadoopClustersCombo = UiUtil.getCombo( "Hadoop cluster" );
            //zookeeper combo
            final ComboBox zkClustersCombo = UiUtil.getCombo( "Zookeeper cluster" );
            //master nodes
            final ComboBox masterNodeCombo = UiUtil.getCombo( "Master node" );
            final ComboBox gcNodeCombo = UiUtil.getCombo( "GC node" );
            final ComboBox monitorNodeCombo = UiUtil.getCombo( "Monitor node" );
            //accumulo init controls
            TextField clusterNameTxtFld = UiUtil.getTextField( "Cluster name", "Cluster name", 20 );
            TextField instanceNameTxtFld = UiUtil.getTextField( "Instance name", "Instance name", 20 );
            TextField passwordTxtFld = UiUtil.getTextField( "Password", "Password", 20 );
            //tracers
            final TwinColSelect tracersSelect =
                    UiUtil.getTwinSelect( "Tracers", "hostname", "Available Nodes", "Selected Nodes", 4 );
            //slave nodes
            final TwinColSelect slavesSelect =
                    UiUtil.getTwinSelect( "Slaves", "hostname", "Available Nodes", "Selected Nodes", 4 );

            //get existing hadoop clusters
            List<HadoopClusterConfig> hadoopClusters = AccumuloUI.getHadoopManager().getClusters();
            //get existing zk clusters
            final List<ZookeeperClusterConfig> zkClusters = AccumuloUI.getZookeeperManager().getClusters();


            //fill zkClustersCombo with zk cluster infos
            if ( zkClusters.size() > 0 ) {
                for ( ZookeeperClusterConfig zookeeperClusterConfig : zkClusters ) {
                    zkClustersCombo.addItem( zookeeperClusterConfig );
                    zkClustersCombo.setItemCaption( zookeeperClusterConfig, zookeeperClusterConfig.getClusterName() );
                }
            }
            //try to find zk cluster info based on one saved in the configuration
            ZookeeperClusterConfig zookeeperClusterConfig =
                    AccumuloUI.getZookeeperManager().getCluster( wizard.getConfig().getZookeeperClusterName() );

            //select if saved found
            if ( zookeeperClusterConfig != null ) {
                zkClustersCombo.setValue( zookeeperClusterConfig );
                zkClustersCombo.setItemCaption( zookeeperClusterConfig, zookeeperClusterConfig.getClusterName() );
            }
            else if ( zkClusters.size() > 0 ) {
                //select first one if saved not found
                zkClustersCombo.setValue( zkClusters.iterator().next() );
            }

            if ( zkClustersCombo.getValue() != null ) {
                ZookeeperClusterConfig zkConfig = ( ZookeeperClusterConfig ) zkClustersCombo.getValue();
                wizard.getConfig().setZookeeperClusterName( zkConfig.getClusterName() );
            }

            zkClustersCombo.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    if ( event.getProperty().getValue() != null ) {
                        ZookeeperClusterConfig zkConfig = ( ZookeeperClusterConfig ) event.getProperty().getValue();
                        wizard.getConfig().setZookeeperClusterName( zkConfig.getClusterName() );
                    }
                }
            } );

            //fill hadoopClustersCombo with hadoop cluster infos
            if ( hadoopClusters.size() > 0 ) {
                for ( HadoopClusterConfig hadoopClusterInfo : hadoopClusters ) {
                    hadoopClustersCombo.addItem( hadoopClusterInfo );
                    hadoopClustersCombo.setItemCaption( hadoopClusterInfo, hadoopClusterInfo.getClusterName() );
                }
            }

            //try to find hadoop cluster info based on one saved in the configuration
            HadoopClusterConfig hadoopClusterConfig =
                    AccumuloUI.getHadoopManager().getCluster( wizard.getConfig().getHadoopClusterName() );

            //select if saved found
            if ( hadoopClusterConfig != null ) {
                hadoopClustersCombo.setValue( hadoopClusterConfig );
                hadoopClustersCombo.setItemCaption( hadoopClusterConfig, hadoopClusterConfig.getClusterName() );
            }
            else if ( hadoopClusters.size() > 0 ) {
                //select first one if saved not found
                hadoopClustersCombo.setValue( hadoopClusters.iterator().next() );
            }


            //fill selection controls with hadoop nodes
            if ( hadoopClustersCombo.getValue() != null ) {
                HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) hadoopClustersCombo.getValue();

                wizard.getConfig().setHadoopClusterName( hadoopInfo.getClusterName() );

                setComboDS( masterNodeCombo, hadoopInfo.getAllNodes() );
                setComboDS( gcNodeCombo, hadoopInfo.getAllNodes() );
                setComboDS( monitorNodeCombo, hadoopInfo.getAllNodes() );
                setTwinSelectDS( tracersSelect, hadoopInfo.getAllNodes() );
                setTwinSelectDS( slavesSelect, hadoopInfo.getAllNodes() );
            }

            //on hadoop cluster change reset all controls and config
            hadoopClustersCombo.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    if ( event.getProperty().getValue() != null ) {
                        HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) event.getProperty().getValue();
                        //reset relevant controls
                        setComboDS( masterNodeCombo, hadoopInfo.getAllNodes() );
                        setComboDS( gcNodeCombo, hadoopInfo.getAllNodes() );
                        setComboDS( monitorNodeCombo, hadoopInfo.getAllNodes() );
                        setTwinSelectDS( tracersSelect, hadoopInfo.getAllNodes() );
                        setTwinSelectDS( slavesSelect, hadoopInfo.getAllNodes() );
                        //reset relevant properties
                        wizard.getConfig().setMasterNode( null );
                        wizard.getConfig().setGcNode( null );
                        wizard.getConfig().setMonitor( null );
                        wizard.getConfig().setTracers( null );
                        wizard.getConfig().setSlaves( null );
                    }
                }
            } );

            //restore master node if back button is pressed
            if ( wizard.getConfig().getMasterNode() != null ) {
                masterNodeCombo.setValue( wizard.getConfig().getMasterNode() );
            }
            //restore gc node if back button is pressed
            if ( wizard.getConfig().getGcNode() != null ) {
                gcNodeCombo.setValue( wizard.getConfig().getGcNode() );
            }
            //restore monitor node if back button is pressed
            if ( wizard.getConfig().getMonitor() != null ) {
                monitorNodeCombo.setValue( wizard.getConfig().getMonitor() );
            }

            //add value change handler
            masterNodeComboChangeListener = new Property.ValueChangeListener() {

                public void valueChange( Property.ValueChangeEvent event ) {
                    if ( event.getProperty().getValue() != null ) {
                        Agent masterNode = ( Agent ) event.getProperty().getValue();
                        wizard.getConfig().setMasterNode( masterNode );
                        HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) hadoopClustersCombo.getValue();
                        List<Agent> hadoopNodes = hadoopInfo.getAllNodes();
                        hadoopNodes.remove( masterNode );
                        gcNodeCombo.removeValueChangeListener( gcNodeComboChangeListener );
                        setComboDS( gcNodeCombo, hadoopNodes );
                        if ( !masterNode.equals( wizard.getConfig().getGcNode() ) ) {
                            gcNodeCombo.setValue( wizard.getConfig().getGcNode() );
                        }
                        else {
                            wizard.getConfig().setGcNode( null );
                        }
                        gcNodeCombo.addValueChangeListener( gcNodeComboChangeListener );
                    }
                }
            };
            masterNodeCombo.addValueChangeListener( masterNodeComboChangeListener );
            //add value change handler
            gcNodeComboChangeListener = new Property.ValueChangeListener() {

                public void valueChange( Property.ValueChangeEvent event ) {
                    if ( event.getProperty().getValue() != null ) {
                        Agent gcNode = ( Agent ) event.getProperty().getValue();
                        wizard.getConfig().setGcNode( gcNode );
                        HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) hadoopClustersCombo.getValue();
                        List<Agent> hadoopNodes = hadoopInfo.getAllNodes();
                        hadoopNodes.remove( gcNode );
                        masterNodeCombo.removeValueChangeListener( masterNodeComboChangeListener );
                        setComboDS( masterNodeCombo, hadoopNodes );
                        if ( !gcNode.equals( wizard.getConfig().getMasterNode() ) ) {
                            masterNodeCombo.setValue( wizard.getConfig().getMasterNode() );
                        }
                        else {
                            wizard.getConfig().setMasterNode( null );
                        }
                        masterNodeCombo.addValueChangeListener( masterNodeComboChangeListener );
                    }
                }
            };
            gcNodeCombo.addValueChangeListener( gcNodeComboChangeListener );
            //add value change handler
            monitorNodeCombo.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    if ( event.getProperty().getValue() != null ) {
                        Agent monitor = ( Agent ) event.getProperty().getValue();
                        wizard.getConfig().setMonitor( monitor );
                    }
                }
            } );

            //restore tracers if back button is pressed
            if ( !Util.isCollectionEmpty( wizard.getConfig().getTracers() ) ) {
                tracersSelect.setValue( wizard.getConfig().getTracers() );
            }
            //restore slaves if back button is pressed
            if ( !Util.isCollectionEmpty( wizard.getConfig().getSlaves() ) ) {
                slavesSelect.setValue( wizard.getConfig().getSlaves() );
            }


            instanceNameTxtFld.setValue( wizard.getConfig().getInstanceName() );
            instanceNameTxtFld.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    wizard.getConfig().setInstanceName( event.getProperty().getValue().toString().trim() );
                }
            } );

            passwordTxtFld.setValue( wizard.getConfig().getPassword() );
            passwordTxtFld.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    wizard.getConfig().setPassword( event.getProperty().getValue().toString().trim() );
                }
            } );


            //add value change handler
            tracersSelect.addValueChangeListener( new Property.ValueChangeListener() {

                public void valueChange( Property.ValueChangeEvent event ) {
                    if ( event.getProperty().getValue() != null ) {
                        Set<Agent> agentList = new HashSet( ( Collection ) event.getProperty().getValue() );
                        wizard.getConfig().setTracers( agentList );
                    }
                }
            } );
            //add value change handler
            slavesSelect.addValueChangeListener( new Property.ValueChangeListener() {

                public void valueChange( Property.ValueChangeEvent event ) {
                    if ( event.getProperty().getValue() != null ) {
                        Set<Agent> agentList = new HashSet( ( Collection ) event.getProperty().getValue() );
                        wizard.getConfig().setSlaves( agentList );
                    }
                }
            } );

            Button next = new Button( "Next" );
            next.addStyleName( "default" );
            //check valid configuration
            next.addClickListener( new Button.ClickListener() {

                @Override
                public void buttonClick( Button.ClickEvent event ) {

                    if ( Strings.isNullOrEmpty( wizard.getConfig().getClusterName() ) ) {
                        show( "Please, enter cluster name" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getConfig().getZookeeperClusterName() ) ) {
                        show( "Please, select Zookeeper cluster" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getConfig().getHadoopClusterName() ) ) {
                        show( "Please, select Hadoop cluster" );
                    }
                    else if ( wizard.getConfig().getMasterNode() == null ) {
                        show( "Please, select master node" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getConfig().getInstanceName() ) ) {
                        show( "Please, specify instance name" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getConfig().getPassword() ) ) {
                        show( "Please, specify password" );
                    }
                    else if ( wizard.getConfig().getGcNode() == null ) {
                        show( "Please, select gc node" );
                    }
                    else if ( wizard.getConfig().getMonitor() == null ) {
                        show( "Please, select monitor" );
                    }
                    else if ( Util.isCollectionEmpty( wizard.getConfig().getTracers() ) ) {
                        show( "Please, select tracer(s)" );
                    }
                    else if ( Util.isCollectionEmpty( wizard.getConfig().getSlaves() ) ) {
                        show( "Please, select slave(s)" );
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


            setSizeFull();

            VerticalLayout content = new VerticalLayout();
            content.setSizeFull();
            content.setSpacing( true );
            content.setMargin( true );

            VerticalLayout layout = new VerticalLayout();
            layout.setSpacing( true );
            layout.addComponent( new Label( "Please, specify installation settings" ) );
            layout.addComponent( content );

            HorizontalLayout masters = new HorizontalLayout();
            masters.setMargin( new MarginInfo( true, false, false, false ) );
            masters.setSpacing( true );
            masters.addComponent( zkClustersCombo );
            masters.addComponent( hadoopClustersCombo );
            masters.addComponent( masterNodeCombo );
            masters.addComponent( gcNodeCombo );
            masters.addComponent( monitorNodeCombo );

            HorizontalLayout credentials = new HorizontalLayout();
            credentials.setMargin( new MarginInfo( true, false, false, false ) );
            credentials.setSpacing( true );
            credentials.addComponent( clusterNameTxtFld );
            credentials.addComponent( instanceNameTxtFld );
            credentials.addComponent( passwordTxtFld );

            HorizontalLayout buttons = new HorizontalLayout();
            buttons.setMargin( new MarginInfo( true, false, false, false ) );
            buttons.setSpacing( true );
            buttons.addComponent( back );
            buttons.addComponent( next );

            content.addComponent( masters );
            content.addComponent( credentials );
            content.addComponent( tracersSelect );
            content.addComponent( slavesSelect );
            content.addComponent( buttons );

            setContent( layout );
        }
        else {
            //Hadoop settings

            final TextField hadoopClusterNameTxtFld = new TextField( "Enter Hadoop cluster name" );
            hadoopClusterNameTxtFld.setInputPrompt( "Hadoop cluster name" );
            hadoopClusterNameTxtFld.setRequired( true );
            hadoopClusterNameTxtFld.setMaxLength( 20 );
            if ( !Strings.isNullOrEmpty( wizard.getHadoopClusterConfig().getClusterName() ) ) {
                hadoopClusterNameTxtFld.setValue( wizard.getHadoopClusterConfig().getClusterName() );
            }
            hadoopClusterNameTxtFld.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    wizard.getHadoopClusterConfig().setClusterName( event.getProperty().getValue().toString().trim() );
                }
            } );

            ComboBox hadoopSlaveNodesComboBox = new ComboBox( "Choose number of Hadoop slave nodes", nodesCountRange );
            hadoopSlaveNodesComboBox.setImmediate( true );
            hadoopSlaveNodesComboBox.setTextInputAllowed( false );
            hadoopSlaveNodesComboBox.setNullSelectionAllowed( false );
            hadoopSlaveNodesComboBox.setValue( wizard.getHadoopClusterConfig().getCountOfSlaveNodes() );

            hadoopSlaveNodesComboBox.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    wizard.getHadoopClusterConfig().setCountOfSlaveNodes( ( Integer ) event.getProperty().getValue() );
                }
            } );

            //configuration replication factor
            ComboBox hadoopReplicationFactorComboBox =
                    new ComboBox( "Choose replication factor for Hadoop slave nodes", nodesCountRange );
            hadoopReplicationFactorComboBox.setImmediate( true );
            hadoopReplicationFactorComboBox.setTextInputAllowed( false );
            hadoopReplicationFactorComboBox.setNullSelectionAllowed( false );
            hadoopReplicationFactorComboBox.setValue( wizard.getHadoopClusterConfig().getReplicationFactor() );

            hadoopReplicationFactorComboBox.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    wizard.getHadoopClusterConfig().setReplicationFactor( ( Integer ) event.getProperty().getValue() );
                }
            } );

            TextField HadoopDomainTxtFld = new TextField( "Enter Hadoop cluster domain name" );
            HadoopDomainTxtFld.setInputPrompt( wizard.getHadoopClusterConfig().getDomainName() );
            HadoopDomainTxtFld.setValue( wizard.getHadoopClusterConfig().getDomainName() );
            HadoopDomainTxtFld.setMaxLength( 20 );
            HadoopDomainTxtFld.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    String value = event.getProperty().getValue().toString().trim();
                    wizard.getHadoopClusterConfig().setDomainName( value );
                }
            } );


            //Zookeeper settings

            GridLayout withHadoopInstallationControls = new GridLayout( 1, 5 );
            withHadoopInstallationControls.setSizeFull();
            withHadoopInstallationControls.setSpacing( true );
            withHadoopInstallationControls.setMargin( true );

            final TextField zkClusterNameTxtFld = new TextField( "Enter Zookeeper cluster name" );
            zkClusterNameTxtFld.setInputPrompt( "Zookeeper cluster name" );
            zkClusterNameTxtFld.setRequired( true );
            zkClusterNameTxtFld.setMaxLength( 20 );
            zkClusterNameTxtFld.setValue( wizard.getConfig().getClusterName() );
            zkClusterNameTxtFld.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    wizard.getConfig().setClusterName( event.getProperty().getValue().toString().trim() );
                }
            } );

            //number of nodes
            ComboBox nodesCountCombo =
                    new ComboBox( "Choose number of Zookeeper nodes", Arrays.asList( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ) );
            nodesCountCombo.setImmediate( true );
            nodesCountCombo.setTextInputAllowed( false );
            nodesCountCombo.setNullSelectionAllowed( false );
            nodesCountCombo.setValue( wizard.getZookeeperClusterConfig().getNumberOfNodes() );

            nodesCountCombo.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    wizard.getZookeeperClusterConfig().setNumberOfNodes( ( Integer ) event.getProperty().getValue() );
                }
            } );

            //Accumulo settings

            final TextField accumuloClusterNameTxtFld = new TextField( "Enter Accumulo cluster name" );
            accumuloClusterNameTxtFld.setInputPrompt( "Accumulo cluster name" );
            accumuloClusterNameTxtFld.setRequired( true );
            accumuloClusterNameTxtFld.setMaxLength( 20 );
            accumuloClusterNameTxtFld.setValue( wizard.getConfig().getClusterName() );
            accumuloClusterNameTxtFld.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    wizard.getConfig().setClusterName( event.getProperty().getValue().toString().trim() );
                }
            } );

            final TextField accumuloInstanceNameTxtFld = new TextField( "Enter instance name" );
            accumuloInstanceNameTxtFld.setInputPrompt( "Instance name" );
            accumuloInstanceNameTxtFld.setRequired( true );
            accumuloInstanceNameTxtFld.setMaxLength( 20 );
            accumuloInstanceNameTxtFld.setValue( wizard.getConfig().getInstanceName() );
            accumuloInstanceNameTxtFld.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    wizard.getConfig().setInstanceName( event.getProperty().getValue().toString().trim() );
                }
            } );

            final TextField accumuloPasswordTxtFld = new TextField( "Enter password" );
            accumuloPasswordTxtFld.setInputPrompt( "Password" );
            accumuloPasswordTxtFld.setRequired( true );
            accumuloPasswordTxtFld.setMaxLength( 20 );
            accumuloPasswordTxtFld.setValue( wizard.getConfig().getPassword() );
            accumuloPasswordTxtFld.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    wizard.getConfig().setPassword( event.getProperty().getValue().toString().trim() );
                }
            } );


            //number of tracers
            ComboBox tracersCountCombo = new ComboBox( "Choose number of Accumulo tracers", nodesCountRange );
            tracersCountCombo.setImmediate( true );
            tracersCountCombo.setTextInputAllowed( false );
            tracersCountCombo.setNullSelectionAllowed( false );
            tracersCountCombo.setValue( wizard.getConfig().getNumberOfTracers() );
            tracersCountCombo.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    wizard.getConfig().setNumberOfTracers( ( Integer ) event.getProperty().getValue() );
                }
            } );

            //number of slaves
            ComboBox slavesCountCombo = new ComboBox( "Choose number of Accumulo slaves", nodesCountRange );
            slavesCountCombo.setImmediate( true );
            slavesCountCombo.setTextInputAllowed( false );
            slavesCountCombo.setNullSelectionAllowed( false );
            slavesCountCombo.setValue( wizard.getConfig().getNumberOfSlaves() );
            slavesCountCombo.addValueChangeListener( new Property.ValueChangeListener() {
                @Override
                public void valueChange( Property.ValueChangeEvent event ) {
                    wizard.getConfig().setNumberOfSlaves( ( Integer ) event.getProperty().getValue() );
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


            Button next = new Button( "Next" );
            next.addStyleName( "default" );
            next.addClickListener( new Button.ClickListener() {

                @Override
                public void buttonClick( Button.ClickEvent event ) {

                    if ( Strings.isNullOrEmpty( wizard.getZookeeperClusterConfig().getClusterName() ) ) {
                        show( "Please provide Zookeeper cluster name" );
                    }
                    else if ( wizard.getZookeeperClusterConfig().getNumberOfNodes() <= 0 ) {
                        show( "Please enter number of ZK nodes" );
                    }
                    else if ( wizard.getZookeeperClusterConfig().getNumberOfNodes()
                            > HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY + wizard.getHadoopClusterConfig()
                                                                                               .getCountOfSlaveNodes
                                                                                                       () ) {
                        show( "Number of ZK nodes must not exceed total number of Hadoop nodes" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getHadoopClusterConfig().getClusterName() ) ) {
                        show( "Please provide Hadoop cluster name" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getHadoopClusterConfig().getDomainName() ) ) {
                        show( "Please provide Hadoop cluster domain name" );
                    }
                    else if ( wizard.getHadoopClusterConfig().getCountOfSlaveNodes() <= 0 ) {
                        show( "Please provide #  of Hadoop slave nodes" );
                    }
                    else if ( wizard.getHadoopClusterConfig().getReplicationFactor() <= 0 ) {
                        show( "Please provide Hadoop cluster replicaton factor" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getConfig().getClusterName() ) ) {
                        show( "Please provide Accumulo cluster name" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getConfig().getInstanceName() ) ) {
                        show( "Please provide Accumulo instance name" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getConfig().getPassword() ) ) {
                        show( "Please provide Accumulo password" );
                    }
                    else if ( wizard.getConfig().getNumberOfTracers() <= 0 ) {
                        show( "Please enter number of Tracer nodes" );
                    }
                    else if ( wizard.getConfig().getNumberOfSlaves() <= 0 ) {
                        show( "Please enter number of Slave nodes" );
                    }
                    else if ( wizard.getConfig().getNumberOfTracers() + wizard.getConfig().getNumberOfSlaves()
                            > HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY + wizard.getHadoopClusterConfig()
                                                                                               .getCountOfSlaveNodes
                                                                                                       () ) {
                        show( "Total number of tracers and slaves must not exceed total number of Hadoop nodes)" );
                    }
                    else {
                        wizard.next();
                    }
                }
            } );

            HorizontalLayout buttons = new HorizontalLayout();
            buttons.addComponent( back );
            buttons.addComponent( next );

            withHadoopInstallationControls.addComponent( new Label(
                    "Please, specify installation settings for combo Hadoop+Zookeeper clusters installation" ) );
            withHadoopInstallationControls.addComponent( new Label( "Zookeeper settings" ) );
            withHadoopInstallationControls.addComponent( accumuloPasswordTxtFld );
            withHadoopInstallationControls.addComponent( slavesCountCombo );
            withHadoopInstallationControls.addComponent( new Label( "Hadoop settings" ) );
            withHadoopInstallationControls.addComponent( hadoopClusterNameTxtFld );
            withHadoopInstallationControls.addComponent( hadoopSlaveNodesComboBox );
            withHadoopInstallationControls.addComponent( hadoopReplicationFactorComboBox );
            withHadoopInstallationControls.addComponent( HadoopDomainTxtFld );
            withHadoopInstallationControls.addComponent( new Label( "Accumulo settings" ) );
            withHadoopInstallationControls.addComponent( accumuloClusterNameTxtFld );
            withHadoopInstallationControls.addComponent( accumuloInstanceNameTxtFld );
            withHadoopInstallationControls.addComponent( accumuloPasswordTxtFld );
            withHadoopInstallationControls.addComponent( tracersCountCombo );
            withHadoopInstallationControls.addComponent( slavesCountCombo );

            withHadoopInstallationControls.addComponent( buttons );

            setContent( withHadoopInstallationControls );
        }
    }


    private void setComboDS( ComboBox target, List<Agent> agents ) {
        target.removeAllItems();
        target.setValue( null );
        for ( Agent agent : agents ) {
            target.addItem( agent );
            target.setItemCaption( agent, agent.getHostname() );
        }
    }


    private void setTwinSelectDS( TwinColSelect target, List<Agent> agents ) {
        target.setValue( null );
        target.setContainerDataSource( new BeanItemContainer<>( Agent.class, agents ) );
    }


    private void show( String notification ) {
        Notification.show( notification );
    }
}
