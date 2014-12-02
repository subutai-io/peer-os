/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.accumulo.ui.wizard;


import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.accumulo.api.SetupType;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

import com.google.common.base.Strings;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.Sizeable;
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


public class ConfigurationStep extends Panel
{
    private Property.ValueChangeListener masterNodeComboChangeListener;
    private Property.ValueChangeListener gcNodeComboChangeListener;
    private EnvironmentManager environmentManager;
    private Wizard wizard;
    private Hadoop hadoop;
    private Zookeeper zookeeper;


    public ConfigurationStep( final Hadoop hadoop, final Zookeeper zookeeper,
                              final EnvironmentManager environmentManager, final Wizard wizard )
    {

        this.environmentManager = environmentManager;
        this.wizard = wizard;
        this.hadoop = hadoop;
        this.zookeeper = zookeeper;

        List<Integer> nodesCountRange =
                ContiguousSet.create( Range.closed( 1, 50 ), DiscreteDomain.integers() ).asList();

        if ( wizard.getConfig().getSetupType() == SetupType.OVER_HADOOP_N_ZK )
        {
            //hadoop combo
            final ComboBox hadoopClustersCombo = getCombo( "Hadoop cluster" );
            hadoopClustersCombo.setId( "hadoopClusterscb" );

            //zookeeper combo
            final ComboBox zkClustersCombo = getCombo( "Zookeeper cluster" );
            zkClustersCombo.setId( "zkClustersCombo" );

            //master nodes
            final ComboBox masterNodeCombo = getCombo( "Master node" );
            masterNodeCombo.setId( "masterNodeCombo" );
            final ComboBox gcNodeCombo = getCombo( "GC node" );
            gcNodeCombo.setId( "gcNodeCombo" );
            final ComboBox monitorNodeCombo = getCombo( "Monitor node" );
            monitorNodeCombo.setId( "monitorNodeCombo" );

            //accumulo init controls
            TextField clusterNameTxtFld = getTextField( "Cluster name", "Cluster name", 20 );
            clusterNameTxtFld.setId( "clusterNameTxtFld" );
            TextField instanceNameTxtFld = getTextField( "Instance name", "Instance name", 20 );
            instanceNameTxtFld.setId( "instanceNameTxtFld" );
            TextField passwordTxtFld = getTextField( "Password", "Password", 20 );
            passwordTxtFld.setId( "passwordTxtFld" );

            //tracers
            final TwinColSelect tracersSelect =
                    getTwinSelect( "Tracers", "hostname", "Available Nodes", "Selected Nodes", 4 );
            tracersSelect.setId( "TracersSelect" );
            //slave nodes
            final TwinColSelect slavesSelect =
                    getTwinSelect( "Slaves", "hostname", "Available Nodes", "Selected Nodes", 4 );
            slavesSelect.setId( "SlavesSelect" );

            //get existing hadoop clusters
            List<HadoopClusterConfig> hadoopClusters = hadoop.getClusters();
            //get existing zk clusters
            final List<ZookeeperClusterConfig> zkClusters = zookeeper.getClusters();


            //fill zkClustersCombo with zk cluster infos
            if ( !zkClusters.isEmpty() )
            {
                for ( ZookeeperClusterConfig zookeeperClusterConfig : zkClusters )
                {
                    zkClustersCombo.addItem( zookeeperClusterConfig );
                    zkClustersCombo.setItemCaption( zookeeperClusterConfig, zookeeperClusterConfig.getClusterName() );
                }
            }
            //try to find zk cluster info based on one saved in the configuration
            ZookeeperClusterConfig zookeeperClusterConfig = null;
            if ( wizard.getConfig().getZookeeperClusterName() != null )
            {
                zookeeperClusterConfig = zookeeper.getCluster( wizard.getConfig().getZookeeperClusterName() );
            }

            //select if saved found
            if ( zookeeperClusterConfig != null )
            {
                zkClustersCombo.setValue( zookeeperClusterConfig );
                zkClustersCombo.setItemCaption( zookeeperClusterConfig, zookeeperClusterConfig.getClusterName() );
            }
            else if ( !zkClusters.isEmpty() )
            {
                //select first one if saved not found
                zkClustersCombo.setValue( zkClusters.iterator().next() );
            }

            if ( zkClustersCombo.getValue() != null )
            {
                ZookeeperClusterConfig zkConfig = ( ZookeeperClusterConfig ) zkClustersCombo.getValue();
                wizard.getConfig().setZookeeperClusterName( zkConfig.getClusterName() );
            }

            zkClustersCombo.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    if ( event.getProperty().getValue() != null )
                    {
                        ZookeeperClusterConfig zkConfig = ( ZookeeperClusterConfig ) event.getProperty().getValue();
                        wizard.getConfig().setZookeeperClusterName( zkConfig.getClusterName() );
                    }
                }
            } );

            //fill hadoopClustersCombo with hadoop cluster infos
            if ( !hadoopClusters.isEmpty() )
            {
                for ( HadoopClusterConfig hadoopClusterInfo : hadoopClusters )
                {
                    hadoopClustersCombo.addItem( hadoopClusterInfo );
                    hadoopClustersCombo.setItemCaption( hadoopClusterInfo, hadoopClusterInfo.getClusterName() );
                }
            }

            //try to find hadoop cluster info based on one saved in the configuration
            HadoopClusterConfig hadoopClusterConfig = null;
            if ( wizard.getConfig().getHadoopClusterName() != null )
            {
                hadoop.getCluster( wizard.getConfig().getHadoopClusterName() );
            }

            //select if saved found
            if ( !hadoopClusters.isEmpty() )
            {
                //select first one if saved not found
                hadoopClustersCombo.setValue( hadoopClusters.iterator().next() );
            }


            // fill selection controls with hadoop nodes
            if ( hadoopClustersCombo.getValue() != null )
            {
                HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) hadoopClustersCombo.getValue();

                wizard.getConfig().setHadoopClusterName( hadoopInfo.getClusterName() );

                setComboDS( masterNodeCombo, hadoopInfo.getAllNodes() );
                setComboDS( gcNodeCombo, hadoopInfo.getAllNodes() );
                setComboDS( monitorNodeCombo, hadoopInfo.getAllNodes() );
                setTwinSelectDS( tracersSelect, getSlaveContainerHosts( Sets.newHashSet( hadoopInfo.getAllNodes() ) ) );
                setTwinSelectDS( slavesSelect, getSlaveContainerHosts( Sets.newHashSet( hadoopInfo.getAllNodes() ) ) );
            }

            //on hadoop cluster change reset all controls and config
            hadoopClustersCombo.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    if ( event.getProperty().getValue() != null )
                    {
                        HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) event.getProperty().getValue();
                        //reset relevant controls
                        setComboDS( masterNodeCombo, hadoopInfo.getAllNodes() );
                        setComboDS( gcNodeCombo, hadoopInfo.getAllNodes() );
                        setComboDS( monitorNodeCombo, hadoopInfo.getAllNodes() );

                        setTwinSelectDS( tracersSelect,
                                getSlaveContainerHosts( Sets.newHashSet( hadoopInfo.getAllNodes() ) ) );
                        setTwinSelectDS( slavesSelect,
                                getSlaveContainerHosts( Sets.newHashSet( hadoopInfo.getAllNodes() ) ) );
                        //reset relevant properties
                        wizard.getConfig().setMasterNode( null );
                        wizard.getConfig().setGcNode( null );
                        wizard.getConfig().setMonitor( null );
                        wizard.getConfig().setTracers( null );
                        wizard.getConfig().setSlaves( null );
                        wizard.getConfig().setHadoopClusterName( hadoopInfo.getClusterName() );
                    }
                }
            } );

            //restore master node if back button is pressed
            if ( wizard.getConfig().getMasterNode() != null )
            {
                masterNodeCombo.setValue( wizard.getConfig().getMasterNode() );
            }
            //restore gc node if back button is pressed
            if ( wizard.getConfig().getGcNode() != null )
            {
                gcNodeCombo.setValue( wizard.getConfig().getGcNode() );
            }
            //restore monitor node if back button is pressed
            if ( wizard.getConfig().getMonitor() != null )
            {
                monitorNodeCombo.setValue( wizard.getConfig().getMonitor() );
            }

            //add value change handler
            masterNodeComboChangeListener = new Property.ValueChangeListener()
            {
                public void valueChange( Property.ValueChangeEvent event )
                {
                    if ( event.getProperty().getValue() != null )
                    {
                        UUID masterNode = ( UUID ) event.getProperty().getValue();
                        wizard.getConfig().setMasterNode( masterNode );
                        HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) hadoopClustersCombo.getValue();
                        List<UUID> hadoopNodes = hadoopInfo.getAllNodes();
                        //                        hadoopNodes.remove( masterNode );
                        gcNodeCombo.removeValueChangeListener( gcNodeComboChangeListener );
                        setComboDS( gcNodeCombo, hadoopNodes );
                        // TODO: we need to edit here if we do not want master machine has some other roles such as
                        // GC, Monitor
                        /*
                        if ( !masterNode.equals( wizard.getConfig().getGcNode() ) )
                        {
                            gcNodeCombo.setValue( wizard.getConfig().getGcNode() );
                        }
                        else
                        {
                            wizard.getConfig().setGcNode( null );
                        }
                        */
                        gcNodeCombo.addValueChangeListener( gcNodeComboChangeListener );
                    }
                }
            };
            masterNodeCombo.addValueChangeListener( masterNodeComboChangeListener );
            //add value change handler
            gcNodeComboChangeListener = new Property.ValueChangeListener()
            {

                public void valueChange( Property.ValueChangeEvent event )
                {
                    if ( event.getProperty().getValue() != null )
                    {
                        UUID gcNode = ( UUID ) event.getProperty().getValue();
                        wizard.getConfig().setGcNode( gcNode );
                        HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) hadoopClustersCombo.getValue();
                        List<UUID> hadoopNodes = hadoopInfo.getAllNodes();
                        //                        hadoopNodes.remove( gcNode );
                        masterNodeCombo.removeValueChangeListener( masterNodeComboChangeListener );
                        // TODO: we need to edit here if we do not want master machine has some other roles such as
                        // GC, Monitor
                        /*
                        setComboDS( masterNodeCombo, hadoopNodes );
                        if ( !gcNode.equals( wizard.getConfig().getMasterNode() ) )
                        {
                            masterNodeCombo.setValue( wizard.getConfig().getMasterNode() );
                        }
                        else
                        {
                            wizard.getConfig().setMasterNode( null );
                        }
                        */
                        masterNodeCombo.addValueChangeListener( masterNodeComboChangeListener );
                    }
                }
            };
            gcNodeCombo.addValueChangeListener( gcNodeComboChangeListener );
            //add value change handler
            monitorNodeCombo.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    if ( event.getProperty().getValue() != null )
                    {
                        UUID monitor = ( UUID ) event.getProperty().getValue();
                        wizard.getConfig().setMonitor( monitor );
                    }
                }
            } );

            //restore tracers if back button is pressed
            if ( !CollectionUtil.isCollectionEmpty( wizard.getConfig().getTracers() ) )
            {
                tracersSelect.setValue( wizard.getConfig().getTracers() );
            }
            //restore slaves if back button is pressed
            if ( !CollectionUtil.isCollectionEmpty( wizard.getConfig().getSlaves() ) )
            {
                slavesSelect.setValue( wizard.getConfig().getSlaves() );
            }

            clusterNameTxtFld.setValue( wizard.getConfig().getInstanceName() );
            clusterNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    wizard.getConfig().setClusterName( event.getProperty().getValue().toString().trim() );
                }
            } );


            instanceNameTxtFld.setValue( wizard.getConfig().getInstanceName() );
            instanceNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    wizard.getConfig().setInstanceName( event.getProperty().getValue().toString().trim() );
                }
            } );

            passwordTxtFld.setValue( wizard.getConfig().getPassword() );
            passwordTxtFld.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    wizard.getConfig().setPassword( event.getProperty().getValue().toString().trim() );
                }
            } );


            //add value change handler
            tracersSelect.addValueChangeListener( new Property.ValueChangeListener()
            {

                public void valueChange( Property.ValueChangeEvent event )
                {
                    if ( event.getProperty().getValue() != null )
                    {
                        Set<UUID> nodes = new HashSet<UUID>();
                        Set<ContainerHost> nodeList = ( Set<ContainerHost> ) event.getProperty().getValue();
                        for ( ContainerHost host : nodeList )
                        {
                            nodes.add( host.getId() );
                        }
                        wizard.getConfig().setTracers( nodes );
                    }
                }
            } );
            //add value change handler
            slavesSelect.addValueChangeListener( new Property.ValueChangeListener()
            {
                public void valueChange( Property.ValueChangeEvent event )
                {
                    if ( event.getProperty().getValue() != null )
                    {
                        Set<UUID> nodes = new HashSet<UUID>();
                        Set<ContainerHost> nodeList = ( Set<ContainerHost> ) event.getProperty().getValue();
                        for ( ContainerHost host : nodeList )
                        {
                            nodes.add( host.getId() );
                        }
                        wizard.getConfig().setSlaves( nodes );
                    }
                }
            } );

            Button next = new Button( "Next" );
            next.setId( "confNext2" );
            next.addStyleName( "default" );
            //check valid configuration
            next.addClickListener( new Button.ClickListener()
            {

                @Override
                public void buttonClick( Button.ClickEvent event )
                {

                    if ( Strings.isNullOrEmpty( wizard.getConfig().getClusterName() ) )
                    {
                        show( "Please, enter cluster name" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getConfig().getZookeeperClusterName() ) )
                    {
                        show( "Please, select Zookeeper cluster" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getConfig().getHadoopClusterName() ) )
                    {
                        show( "Please, select Hadoop cluster" );
                    }
                    else if ( wizard.getConfig().getMasterNode() == null )
                    {
                        show( "Please, select master node" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getConfig().getInstanceName() ) )
                    {
                        show( "Please, specify instance name" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getConfig().getPassword() ) )
                    {
                        show( "Please, specify password" );
                    }
                    else if ( wizard.getConfig().getGcNode() == null )
                    {
                        show( "Please, select gc node" );
                    }
                    else if ( wizard.getConfig().getMonitor() == null )
                    {
                        show( "Please, select monitor" );
                    }
                    else if ( CollectionUtil.isCollectionEmpty( wizard.getConfig().getTracers() ) )
                    {
                        show( "Please, select tracer(s)" );
                    }
                    else if ( CollectionUtil.isCollectionEmpty( wizard.getConfig().getSlaves() ) )
                    {
                        show( "Please, select slave(s)" );
                    }
                    else
                    {
                        wizard.next();
                    }
                }
            } );

            Button back = new Button( "Back" );
            back.setId( "confBack2" );
            back.addStyleName( "default" );
            back.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent event )
                {
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
        else
        {
            //Hadoop settings

            final TextField hadoopClusterNameTxtFld = new TextField( "Enter Hadoop cluster name" );
            hadoopClusterNameTxtFld.setId( "hadoopClusterNameTxtFld" );
            hadoopClusterNameTxtFld.setInputPrompt( "Hadoop cluster name" );
            hadoopClusterNameTxtFld.setRequired( true );
            hadoopClusterNameTxtFld.setMaxLength( 20 );
            if ( !Strings.isNullOrEmpty( wizard.getHadoopClusterConfig().getClusterName() ) )
            {
                hadoopClusterNameTxtFld.setValue( wizard.getHadoopClusterConfig().getClusterName() );
            }
            hadoopClusterNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    wizard.getHadoopClusterConfig().setClusterName( event.getProperty().getValue().toString().trim() );
                }
            } );

            ComboBox hadoopSlaveNodesComboBox = new ComboBox( "Choose number of Hadoop slave nodes", nodesCountRange );
            hadoopSlaveNodesComboBox.setId( "hadoopSlaveNodesComboBox" );
            hadoopSlaveNodesComboBox.setImmediate( true );
            hadoopSlaveNodesComboBox.setTextInputAllowed( false );
            hadoopSlaveNodesComboBox.setNullSelectionAllowed( false );
            hadoopSlaveNodesComboBox.setValue( wizard.getHadoopClusterConfig().getCountOfSlaveNodes() );

            hadoopSlaveNodesComboBox.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    wizard.getHadoopClusterConfig().setCountOfSlaveNodes( ( Integer ) event.getProperty().getValue() );
                }
            } );

            //configuration replication factor
            ComboBox hadoopReplicationFactorComboBox =
                    new ComboBox( "Choose replication factor for Hadoop slave nodes", nodesCountRange );
            hadoopReplicationFactorComboBox.setId( "hadoopReplicationFactorComboBox" );
            hadoopReplicationFactorComboBox.setImmediate( true );
            hadoopReplicationFactorComboBox.setTextInputAllowed( false );
            hadoopReplicationFactorComboBox.setNullSelectionAllowed( false );
            hadoopReplicationFactorComboBox.setValue( wizard.getHadoopClusterConfig().getReplicationFactor() );

            hadoopReplicationFactorComboBox.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    wizard.getHadoopClusterConfig().setReplicationFactor( ( Integer ) event.getProperty().getValue() );
                }
            } );

            TextField HadoopDomainTxtFld = new TextField( "Enter Hadoop cluster domain name" );
            HadoopDomainTxtFld.setId( "HadoopDomainTxtFld" );
            HadoopDomainTxtFld.setInputPrompt( wizard.getHadoopClusterConfig().getDomainName() );
            HadoopDomainTxtFld.setValue( wizard.getHadoopClusterConfig().getDomainName() );
            HadoopDomainTxtFld.setMaxLength( 20 );
            HadoopDomainTxtFld.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    String value = event.getProperty().getValue().toString().trim();
                    wizard.getHadoopClusterConfig().setDomainName( value );
                }
            } );


            //Zookeeper settings

            final TextField zkClusterNameTxtFld = new TextField( "Enter Zookeeper cluster name" );
            zkClusterNameTxtFld.setId( "zkClusterNameTxtFld" );
            zkClusterNameTxtFld.setInputPrompt( "Zookeeper cluster name" );
            zkClusterNameTxtFld.setRequired( true );
            zkClusterNameTxtFld.setMaxLength( 20 );
            zkClusterNameTxtFld.setValue( wizard.getZookeeperClusterConfig().getClusterName() );
            zkClusterNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    wizard.getZookeeperClusterConfig()
                          .setClusterName( event.getProperty().getValue().toString().trim() );
                }
            } );

            //number of nodes
            ComboBox zkNodesCountCombo =
                    new ComboBox( "Choose number of Zookeeper nodes", Arrays.asList( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ) );
            zkNodesCountCombo.setId( "zkNodesCountCombo" );
            zkNodesCountCombo.setImmediate( true );
            zkNodesCountCombo.setTextInputAllowed( false );
            zkNodesCountCombo.setNullSelectionAllowed( false );
            zkNodesCountCombo.setValue( wizard.getZookeeperClusterConfig().getNumberOfNodes() );

            zkNodesCountCombo.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    wizard.getZookeeperClusterConfig().setNumberOfNodes( ( Integer ) event.getProperty().getValue() );
                }
            } );

            //Accumulo settings

            final TextField accumuloClusterNameTxtFld = new TextField( "Enter Accumulo cluster name" );
            accumuloClusterNameTxtFld.setId( "accumuloClusterNameTxtFld" );
            accumuloClusterNameTxtFld.setInputPrompt( "Accumulo cluster name" );
            accumuloClusterNameTxtFld.setRequired( true );
            accumuloClusterNameTxtFld.setMaxLength( 20 );
            accumuloClusterNameTxtFld.setValue( wizard.getConfig().getClusterName() );
            accumuloClusterNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    wizard.getConfig().setClusterName( event.getProperty().getValue().toString().trim() );
                }
            } );

            final TextField accumuloInstanceNameTxtFld = new TextField( "Enter instance name" );
            accumuloInstanceNameTxtFld.setId( "accumuloInstanceNameTxtFld" );
            accumuloInstanceNameTxtFld.setInputPrompt( "Instance name" );
            accumuloInstanceNameTxtFld.setRequired( true );
            accumuloInstanceNameTxtFld.setMaxLength( 20 );
            accumuloInstanceNameTxtFld.setValue( wizard.getConfig().getInstanceName() );
            accumuloInstanceNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    wizard.getConfig().setInstanceName( event.getProperty().getValue().toString().trim() );
                }
            } );

            final TextField accumuloPasswordTxtFld = new TextField( "Enter password" );
            accumuloPasswordTxtFld.setId( "accumuloPasswordTxtFld" );
            accumuloPasswordTxtFld.setInputPrompt( "Password" );
            accumuloPasswordTxtFld.setRequired( true );
            accumuloPasswordTxtFld.setMaxLength( 20 );
            accumuloPasswordTxtFld.setValue( wizard.getConfig().getPassword() );
            accumuloPasswordTxtFld.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    wizard.getConfig().setPassword( event.getProperty().getValue().toString().trim() );
                }
            } );


            //number of tracers
            ComboBox accumuloTracersCountCombo = new ComboBox( "Choose number of Accumulo tracers", nodesCountRange );
            accumuloTracersCountCombo.setId( "accumuloTracersCountCombo" );
            accumuloTracersCountCombo.setImmediate( true );
            accumuloTracersCountCombo.setTextInputAllowed( false );
            accumuloTracersCountCombo.setNullSelectionAllowed( false );
            accumuloTracersCountCombo.setValue( wizard.getConfig().getNumberOfTracers() );
            accumuloTracersCountCombo.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    wizard.getConfig().setNumberOfTracers( ( Integer ) event.getProperty().getValue() );
                }
            } );

            //number of slaves
            ComboBox accumuloSlavesCountCombo = new ComboBox( "Choose number of Accumulo slaves", nodesCountRange );
            accumuloSlavesCountCombo.setId( "accumuloSlavesCountCombo" );
            accumuloSlavesCountCombo.setImmediate( true );
            accumuloSlavesCountCombo.setTextInputAllowed( false );
            accumuloSlavesCountCombo.setNullSelectionAllowed( false );
            accumuloSlavesCountCombo.setValue( wizard.getConfig().getNumberOfSlaves() );
            accumuloSlavesCountCombo.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    wizard.getConfig().setNumberOfSlaves( ( Integer ) event.getProperty().getValue() );
                }
            } );


            Button back = new Button( "Back" );
            back.setId( "confBack3" );
            back.setId( "ConfBack" );
            back.addStyleName( "default" );
            back.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    wizard.back();
                }
            } );


            Button next = new Button( "Next" );
            next.setId( "confNext3" );
            next.setId( "ConfNext" );
            next.addStyleName( "default" );
            next.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    wizard.getConfig().setHadoopClusterName( wizard.getHadoopClusterConfig().getClusterName() );
                    wizard.getConfig().setZookeeperClusterName( wizard.getZookeeperClusterConfig().getClusterName() );

                    if ( Strings.isNullOrEmpty( wizard.getZookeeperClusterConfig().getClusterName() ) )
                    {
                        show( "Please provide Zookeeper cluster name" );
                    }
                    else if ( wizard.getZookeeperClusterConfig().getNumberOfNodes() <= 0 )
                    {
                        show( "Please enter number of ZK nodes" );
                    }
                    else if ( wizard.getZookeeperClusterConfig().getNumberOfNodes()
                            > HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY + wizard.getHadoopClusterConfig()
                                                                                               .getCountOfSlaveNodes() )
                    {
                        show( "Number of ZK nodes must not exceed total number of Hadoop nodes" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getHadoopClusterConfig().getClusterName() ) )
                    {
                        show( "Please provide Hadoop cluster name" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getHadoopClusterConfig().getDomainName() ) )
                    {
                        show( "Please provide Hadoop cluster domain name" );
                    }
                    else if ( wizard.getHadoopClusterConfig().getCountOfSlaveNodes() <= 0 )
                    {
                        show( "Please provide #  of Hadoop slave nodes" );
                    }
                    else if ( wizard.getHadoopClusterConfig().getReplicationFactor() <= 0 )
                    {
                        show( "Please provide Hadoop cluster replicaton factor" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getConfig().getClusterName() ) )
                    {
                        show( "Please provide Accumulo cluster name" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getConfig().getInstanceName() ) )
                    {
                        show( "Please provide Accumulo instance name" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getConfig().getPassword() ) )
                    {
                        show( "Please provide Accumulo password" );
                    }
                    else if ( wizard.getConfig().getNumberOfTracers() <= 0 )
                    {
                        show( "Please enter number of Tracer nodes" );
                    }
                    else if ( wizard.getConfig().getNumberOfSlaves() <= 0 )
                    {
                        show( "Please enter number of Slave nodes" );
                    }
                    else if ( wizard.getConfig().getNumberOfTracers() + wizard.getConfig().getNumberOfSlaves()
                            > HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY + wizard.getHadoopClusterConfig()
                                                                                               .getCountOfSlaveNodes() )
                    {
                        show( "Total number of tracers and slaves must not exceed total number of Hadoop nodes)" );
                    }
                    else
                    {
                        wizard.next();
                    }
                }
            } );

            HorizontalLayout buttons = new HorizontalLayout();
            buttons.addComponent( back );
            buttons.addComponent( next );

            GridLayout withHadoopInstallationControls = new GridLayout( 1, 5 );
            withHadoopInstallationControls.setSizeFull();
            withHadoopInstallationControls.setSpacing( true );
            withHadoopInstallationControls.setMargin( true );

            withHadoopInstallationControls.addComponent( new Label(
                    "Please, specify installation settings for combo Hadoop+Zookeeper+Accumulo clusters installation"
            ) );
            withHadoopInstallationControls.addComponent( new Label( "Zookeeper settings" ) );
            withHadoopInstallationControls.addComponent( zkClusterNameTxtFld );
            withHadoopInstallationControls.addComponent( zkNodesCountCombo );
            withHadoopInstallationControls.addComponent( new Label( "Hadoop settings" ) );
            withHadoopInstallationControls.addComponent( hadoopClusterNameTxtFld );
            withHadoopInstallationControls.addComponent( hadoopSlaveNodesComboBox );
            withHadoopInstallationControls.addComponent( hadoopReplicationFactorComboBox );
            withHadoopInstallationControls.addComponent( HadoopDomainTxtFld );
            withHadoopInstallationControls.addComponent( new Label( "Accumulo settings" ) );
            withHadoopInstallationControls.addComponent( accumuloClusterNameTxtFld );
            withHadoopInstallationControls.addComponent( accumuloInstanceNameTxtFld );
            withHadoopInstallationControls.addComponent( accumuloPasswordTxtFld );
            withHadoopInstallationControls.addComponent( accumuloTracersCountCombo );
            withHadoopInstallationControls.addComponent( accumuloSlavesCountCombo );

            withHadoopInstallationControls.addComponent( buttons );

            setContent( withHadoopInstallationControls );
        }
    }


    private Set<ContainerHost> getSlaveContainerHosts( Set<UUID> slaves )
    {
        Set<ContainerHost> set = new HashSet<>();
        for ( UUID uuid : slaves )
        {
            set.add( environmentManager.getEnvironmentByUUID(
                    hadoop.getCluster( wizard.getConfig().getHadoopClusterName() ).getEnvironmentId() )
                                       .getContainerHostById( uuid ) );
        }
        return set;
    }


    public static ComboBox getCombo( String title )
    {
        ComboBox combo = new ComboBox( title );
        combo.setImmediate( true );
        combo.setTextInputAllowed( false );
        combo.setRequired( true );
        combo.setNullSelectionAllowed( false );
        return combo;
    }


    public static TwinColSelect getTwinSelect( String title, String captionProperty, String leftTitle,
                                               String rightTitle, int rows )
    {
        TwinColSelect twinColSelect = new TwinColSelect( title );
        twinColSelect.setItemCaptionPropertyId( captionProperty );
        twinColSelect.setRows( rows );
        twinColSelect.setMultiSelect( true );
        twinColSelect.setImmediate( true );
        twinColSelect.setLeftColumnCaption( leftTitle );
        twinColSelect.setRightColumnCaption( rightTitle );
        twinColSelect.setWidth( 100, Sizeable.Unit.PERCENTAGE );
        twinColSelect.setRequired( true );
        return twinColSelect;
    }


    public static TextField getTextField( String caption, String prompt, int maxLength )
    {
        TextField textField = new TextField( caption );
        textField.setInputPrompt( prompt );
        textField.setMaxLength( maxLength );
        textField.setRequired( true );
        return textField;
    }


    private void setComboDS( ComboBox target, List<UUID> agents )
    {
        target.removeAllItems();
        target.setValue( null );
        for ( UUID agent : agents )
        {
            ContainerHost host = getHost( agent );
            target.addItem( host.getId() );
            target.setItemCaption( host.getId(), host.getHostname() );
        }
    }


    private ContainerHost getHost( UUID uuid )
    {
        return environmentManager.getEnvironmentByUUID(
                hadoop.getCluster( wizard.getConfig().getHadoopClusterName() ).getEnvironmentId() )
                                 .getContainerHostById( uuid );
    }


    private void setTwinSelectDS( TwinColSelect target, Set<ContainerHost> containerHosts )
    {
        target.setValue( null );
        target.setContainerDataSource( new BeanItemContainer<>( ContainerHost.class, containerHosts ) );
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }
}
