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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;

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


public class ConfigurationStep extends Panel
{

    public ConfigurationStep( final Hadoop hadoop, final Wizard wizard )
    {

        if ( wizard.getConfig().getSetupType() == SetupType.STANDALONE )
        {
            GridLayout standaloneInstallationControls = new GridLayout( 1, 5 );
            standaloneInstallationControls.setSizeFull();
            standaloneInstallationControls.setSpacing( true );
            standaloneInstallationControls.setMargin( true );

            final TextField clusterNameTxtFld = new TextField( "Enter cluster name" );
            clusterNameTxtFld.setId( "ZookeeperConfClusterName" );
            clusterNameTxtFld.setInputPrompt( "Cluster name" );
            clusterNameTxtFld.setRequired( true );
            clusterNameTxtFld.setMaxLength( 20 );
            clusterNameTxtFld.setValue( wizard.getConfig().getClusterName() );
            clusterNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    wizard.getConfig().setClusterName( event.getProperty().getValue().toString().trim() );
                }
            } );

            //number of nodes
            ComboBox nodesCountCombo =
                    new ComboBox( "Choose number of nodes", Arrays.asList( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ) );
            nodesCountCombo.setId( "ZookeeperNumNodes" );
            nodesCountCombo.setImmediate( true );
            nodesCountCombo.setTextInputAllowed( false );
            nodesCountCombo.setNullSelectionAllowed( false );
            nodesCountCombo.setValue( wizard.getConfig().getNumberOfNodes() );

            nodesCountCombo.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    wizard.getConfig().setNumberOfNodes( ( Integer ) event.getProperty().getValue() );
                }
            } );

            Button next = new Button( "Next" );
            next.setId( "ZookeeperConfNext" );
            next.addStyleName( "default" );
            next.addClickListener( new Button.ClickListener()
            {

                @Override
                public void buttonClick( Button.ClickEvent event )
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
            back.setId( "ZookeeperConfBack" );
            back.addStyleName( "default" );
            back.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    wizard.back();
                }
            } );


            HorizontalLayout buttons = new HorizontalLayout();
            buttons.addComponent( back );
            buttons.addComponent( next );

            standaloneInstallationControls.addComponent(
                    new Label( "Please, specify installation settings for standalone cluster installation" ) );
            standaloneInstallationControls.addComponent( clusterNameTxtFld );
            standaloneInstallationControls.addComponent( nodesCountCombo );
            standaloneInstallationControls.addComponent( buttons );

            setContent( standaloneInstallationControls );
        }
        else if ( wizard.getConfig().getSetupType() == SetupType.OVER_HADOOP )
        {
            GridLayout overHadoopInstallationControls = new GridLayout( 1, 5 );
            overHadoopInstallationControls.setSizeFull();
            overHadoopInstallationControls.setSpacing( true );
            overHadoopInstallationControls.setMargin( true );

            final TextField clusterNameTxtFld = new TextField( "Enter cluster name" );
            clusterNameTxtFld.setId( "zookeeperClusterName" );
            clusterNameTxtFld.setInputPrompt( "Cluster name" );
            clusterNameTxtFld.setRequired( true );
            clusterNameTxtFld.setMaxLength( 20 );
            clusterNameTxtFld.setValue( wizard.getConfig().getClusterName() );
            clusterNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    wizard.getConfig().setClusterName( event.getProperty().getValue().toString().trim() );
                }
            } );

            ComboBox hadoopClustersCombo = new ComboBox( "Hadoop cluster" );
            hadoopClustersCombo.setId( "ZookeeperConfHadoopCluster" );

            final TwinColSelect hadoopNodesSelect = new TwinColSelect( "Nodes", new ArrayList<ContainerHost>() );
            hadoopNodesSelect.setId( "ZookeeperConfHadoopNodesSelection" );

            hadoopClustersCombo.setImmediate( true );
            hadoopClustersCombo.setTextInputAllowed( false );
            hadoopClustersCombo.setRequired( true );
            hadoopClustersCombo.setNullSelectionAllowed( false );

            List<HadoopClusterConfig> hadoopClusterConfigs = hadoop.getClusters();
            if ( !hadoopClusterConfigs.isEmpty() )
            {
                for ( HadoopClusterConfig hadoopClusterInfo : hadoopClusterConfigs )
                {
                    hadoopClustersCombo.addItem( hadoopClusterInfo );
                    hadoopClustersCombo.setItemCaption( hadoopClusterInfo, hadoopClusterInfo.getClusterName() );
                }
            }

            HadoopClusterConfig info = null;
            if ( wizard.getConfig().getHadoopClusterName() != null )
            {
                info = hadoop.getCluster( wizard.getConfig().getHadoopClusterName() );
            }

            if ( info != null )
            {
                hadoopClustersCombo.setValue( info );
            }
            else if ( !hadoopClusterConfigs.isEmpty() )
            {
                hadoopClustersCombo.setValue( hadoopClusterConfigs.iterator().next() );
            }

            if ( hadoopClustersCombo.getValue() != null )
            {
                HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) hadoopClustersCombo.getValue();
                wizard.getConfig().setHadoopClusterName( hadoopInfo.getClusterName() );
                hadoopNodesSelect
                        .setContainerDataSource( new BeanItemContainer<>( ContainerHost.class, hadoopInfo.getAllNodes() ) );
            }

            hadoopClustersCombo.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    if ( event.getProperty().getValue() != null )
                    {
                        HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) event.getProperty().getValue();
                        hadoopNodesSelect.setValue( null );
                        hadoopNodesSelect.setContainerDataSource(
                                new BeanItemContainer<>( ContainerHost.class, hadoopInfo.getAllNodes() ) );
                        wizard.getConfig().setHadoopClusterName( hadoopInfo.getClusterName() );
                        wizard.getConfig().setNodes( new HashSet<ContainerHost>() );
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

            if ( !CollectionUtil.isCollectionEmpty( wizard.getConfig().getNodes() ) )
            {
                hadoopNodesSelect.setValue( wizard.getConfig().getNodes() );
            }
            hadoopNodesSelect.addValueChangeListener( new Property.ValueChangeListener()
            {

                public void valueChange( Property.ValueChangeEvent event )
                {
                    if ( event.getProperty().getValue() != null )
                    {
                        Set<ContainerHost> containerHosts = new HashSet<>( ( Collection<ContainerHost> ) event.getProperty().getValue() );
                        wizard.getConfig().setNodes( containerHosts );
                    }
                }
            } );

            Button next = new Button( "Next" );
            next.setId( "ZookeeperConfNext" );
            next.addStyleName( "default" );
            next.addClickListener( new Button.ClickListener()
            {

                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    if ( Strings.isNullOrEmpty( wizard.getConfig().getClusterName() ) )
                    {
                        show( "Please, prvide cluster name" );
                    }
                    else if ( Strings.isNullOrEmpty( wizard.getConfig().getHadoopClusterName() ) )
                    {
                        show( "Please, select Hadoop cluster" );
                    }
                    else if ( CollectionUtil.isCollectionEmpty( wizard.getConfig().getNodes() ) )
                    {
                        show( "Please, select zk nodes" );
                    }
                    else
                    {
                        wizard.next();
                    }
                }
            } );

            Button back = new Button( "Back" );
            back.setId( "ZookeeperConfBack" );
            back.addStyleName( "default" );
            back.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    wizard.back();
                }
            } );

            HorizontalLayout buttons = new HorizontalLayout();
            buttons.addComponent( back );
            buttons.addComponent( next );

            overHadoopInstallationControls.addComponent(
                    new Label( "Please, specify installation settings for over-Hadoop cluster installation" ) );
            overHadoopInstallationControls.addComponent( clusterNameTxtFld );
            overHadoopInstallationControls.addComponent( hadoopClustersCombo );
            overHadoopInstallationControls.addComponent( hadoopNodesSelect );
            overHadoopInstallationControls.addComponent( buttons );
            setContent( overHadoopInstallationControls );
        }
        else if ( wizard.getConfig().getSetupType() == SetupType.WITH_HADOOP )
        {
            //Hadoop+Zookeeper combo template based cluster installation controls

            //Hadoop settings

            final TextField hadoopClusterNameTxtFld = new TextField( "Enter Hadoop cluster name" );
            hadoopClusterNameTxtFld.setId( "ZookeeperConfHadoopCluster" );
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

            //configuration servers number
            List<Integer> count = new ArrayList<>();
            for ( int i = 1; i < 50; i++ )
            {
                count.add( i );
            }

            ComboBox hadoopSlaveNodesComboBox = new ComboBox( "Choose number of Hadoop slave nodes", count );
            hadoopSlaveNodesComboBox.setId( "ZookeeperConfHadoopNodesSelection" );
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
                    new ComboBox( "Choose replication factor for Hadoop slave nodes", count );
            hadoopReplicationFactorComboBox.setId( "ZookeeperConfHadoopReplFactor" );
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
            HadoopDomainTxtFld.setId( "ZookeeperConfHadoopDomain" );
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

            GridLayout withHadoopInstallationControls = new GridLayout( 1, 5 );
            withHadoopInstallationControls.setSizeFull();
            withHadoopInstallationControls.setSpacing( true );
            withHadoopInstallationControls.setMargin( true );

            final TextField clusterNameTxtFld = new TextField( "Enter Zookeeper cluster name" );
            clusterNameTxtFld.setId( "ZookeeperConfClusterName" );
            clusterNameTxtFld.setInputPrompt( "Zookeeper cluster name" );
            clusterNameTxtFld.setRequired( true );
            clusterNameTxtFld.setMaxLength( 20 );
            clusterNameTxtFld.setValue( wizard.getConfig().getClusterName() );
            clusterNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    wizard.getConfig().setClusterName( event.getProperty().getValue().toString().trim() );
                }
            } );

            //number of nodes
            ComboBox nodesCountCombo =
                    new ComboBox( "Choose number of Zookeeper nodes", Arrays.asList( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ) );
            nodesCountCombo.setId( "ZookeeperConfNodesNum" );
            nodesCountCombo.setImmediate( true );
            nodesCountCombo.setTextInputAllowed( false );
            nodesCountCombo.setNullSelectionAllowed( false );
            nodesCountCombo.setValue( wizard.getConfig().getNumberOfNodes() );

            nodesCountCombo.addValueChangeListener( new Property.ValueChangeListener()
            {
                @Override
                public void valueChange( Property.ValueChangeEvent event )
                {
                    wizard.getConfig().setNumberOfNodes( ( Integer ) event.getProperty().getValue() );
                }
            } );

            Button next = new Button( "Next" );
            next.setId( "ZookeeperConfNext" );
            next.addStyleName( "default" );
            next.addClickListener( new Button.ClickListener()
            {

                @Override
                public void buttonClick( Button.ClickEvent event )
                {

                    if ( Strings.isNullOrEmpty( wizard.getConfig().getClusterName() ) )
                    {
                        show( "Please provide Zookeeper cluster name" );
                    }
                    else if ( wizard.getConfig().getNumberOfNodes() <= 0 )
                    {
                        show( "Please enter number of ZK nodes" );
                    }
                    else if ( wizard.getConfig().getNumberOfNodes()
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
                        show( "Please provide number of Hadoop slave nodes" );
                    }
                    else if ( wizard.getHadoopClusterConfig().getReplicationFactor() <= 0 )
                    {
                        show( "Please provide Hadoop cluster replicaton factor" );
                    }
                    else
                    {
                        wizard.next();
                    }
                }
            } );

            Button back = new Button( "Back" );
            back.setId( "ZookeeperConfBack" );
            back.addStyleName( "default" );
            back.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    wizard.back();
                }
            } );


            HorizontalLayout buttons = new HorizontalLayout();
            buttons.addComponent( back );
            buttons.addComponent( next );


            withHadoopInstallationControls.addComponent( new Label(
                    "Please, specify installation settings for combo Hadoop+Zookeeper clusters installation" ) );
            withHadoopInstallationControls.addComponent( new Label( "Zookeeper settings" ) );
            withHadoopInstallationControls.addComponent( clusterNameTxtFld );
            withHadoopInstallationControls.addComponent( nodesCountCombo );
            withHadoopInstallationControls.addComponent( new Label( "Hadoop settings" ) );
            withHadoopInstallationControls.addComponent( hadoopClusterNameTxtFld );
            withHadoopInstallationControls.addComponent( hadoopSlaveNodesComboBox );
            withHadoopInstallationControls.addComponent( hadoopReplicationFactorComboBox );
            withHadoopInstallationControls.addComponent( HadoopDomainTxtFld );

            withHadoopInstallationControls.addComponent( buttons );

            setContent( withHadoopInstallationControls );
        }
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }
}
