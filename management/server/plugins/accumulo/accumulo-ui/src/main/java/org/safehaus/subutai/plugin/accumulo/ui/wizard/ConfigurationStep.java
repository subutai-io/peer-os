/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.accumulo.ui.wizard;


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
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
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
