/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.accumulo.wizard;


import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.ui.accumulo.AccumuloUI;
import org.safehaus.subutai.ui.accumulo.common.UiUtil;

import java.util.*;


/**
 * @author dilshat
 */
public class ConfigurationStep extends Panel
{
    final Property.ValueChangeListener masterNodeComboChangeListener;
    final Property.ValueChangeListener gcNodeComboChangeListener;


    public ConfigurationStep( final Wizard wizard )
    {

        //hadoop combo
        final ComboBox hadoopClustersCombo = UiUtil.getCombo( "Hadoop cluster" );
        //master nodes
        final ComboBox masterNodeCombo = UiUtil.getCombo( "Master node" );
        final ComboBox gcNodeCombo = UiUtil.getCombo( "GC node" );
        final ComboBox monitorNodeCombo = UiUtil.getCombo( "Monitor node" );
        //accumulo init controls
        TextField instanceNameTxtFld = UiUtil.getTextField( "Instance name", "Instance name", 20 );
        final PasswordField passwordField = UiUtil.getPassField( "Password" );
        final PasswordField confirmPass = UiUtil.getPassField( "Confirm Password" );

        //tracers
        final TwinColSelect tracersSelect =
            UiUtil.getTwinSelect( "Tracers", "hostname", "Available Nodes", "Selected Nodes", 4 );
        //slave nodes
        final TwinColSelect slavesSelect =
            UiUtil.getTwinSelect( "Slaves", "hostname", "Available Nodes", "Selected Nodes", 4 );

        //get hadoop clusters from db
        List<Config> hadoopClusters = AccumuloUI.getHadoopManager().getClusters();
        final List<org.safehaus.subutai.api.zookeeper.Config> zkClusters =
            AccumuloUI.getZookeeperManager().getClusters();
        Set<Config> filteredHadoopClusters = new HashSet<>();

        //filter out those hadoop clusters which have zk clusters installed on top
        for ( Config hadoopClusterInfo : hadoopClusters )
        {
            for ( org.safehaus.subutai.api.zookeeper.Config zkClusterInfo : zkClusters )
            {
                if ( hadoopClusterInfo.getClusterName().equals( zkClusterInfo.getClusterName() ) && !zkClusterInfo
                    .isStandalone() )
                {
                    filteredHadoopClusters.add( hadoopClusterInfo );
                    break;
                }
            }
        }

        //fill hadoopClustersCombo with hadoop cluster infos
        if ( filteredHadoopClusters.size() > 0 )
        {
            for ( Config hadoopClusterInfo : filteredHadoopClusters )
            {
                hadoopClustersCombo.addItem( hadoopClusterInfo );
                hadoopClustersCombo.setItemCaption( hadoopClusterInfo, hadoopClusterInfo.getClusterName() );
            }
        }

        //try to find hadoop cluster info based on one saved in the configuration
        Config info =
            AccumuloUI.getHadoopManager().getCluster( wizard.getConfig().getClusterName() );

        //select if saved found
        if ( info != null )
        {
            hadoopClustersCombo.setValue( info );
            hadoopClustersCombo.setItemCaption( info, info.getClusterName() );
        }
        else if ( filteredHadoopClusters.size() > 0 )
        {
            //select first one if saved not found
            hadoopClustersCombo.setValue( filteredHadoopClusters.iterator().next() );
        }

        //fill selection controls with hadoop nodes
        if ( hadoopClustersCombo.getValue() != null )
        {
            Config hadoopInfo =
                ( Config ) hadoopClustersCombo.getValue();

            wizard.getConfig().setClusterName( hadoopInfo.getClusterName() );

            setComboDS( masterNodeCombo, filterAgents( hadoopInfo, zkClusters ) );
            setComboDS( gcNodeCombo, filterAgents( hadoopInfo, zkClusters ) );
            setComboDS( monitorNodeCombo, filterAgents( hadoopInfo, zkClusters ) );
            setTwinSelectDS( tracersSelect, filterAgents( hadoopInfo, zkClusters ) );
            setTwinSelectDS( slavesSelect, filterAgents( hadoopInfo, zkClusters ) );
        }

        //on hadoop cluster change reset all controls and config
        hadoopClustersCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    Config hadoopInfo =
                        ( Config ) event.getProperty().getValue();
                    setComboDS( masterNodeCombo, filterAgents( hadoopInfo, zkClusters ) );
                    setComboDS( gcNodeCombo, filterAgents( hadoopInfo, zkClusters ) );
                    setComboDS( monitorNodeCombo, filterAgents( hadoopInfo, zkClusters ) );
                    setTwinSelectDS( tracersSelect, filterAgents( hadoopInfo, zkClusters ) );
                    setTwinSelectDS( slavesSelect, filterAgents( hadoopInfo, zkClusters ) );
                    wizard.getConfig().reset();
                    wizard.getConfig().setClusterName( hadoopInfo.getClusterName() );
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
                    Agent masterNode = ( Agent ) event.getProperty().getValue();
                    wizard.getConfig().setMasterNode( masterNode );
                    Config hadoopInfo =
                        ( Config ) hadoopClustersCombo.getValue();
                    List<Agent> hadoopNodes = filterAgents( hadoopInfo, zkClusters );
                    hadoopNodes.remove( masterNode );
                    gcNodeCombo.removeValueChangeListener( gcNodeComboChangeListener );
                    setComboDS( gcNodeCombo, hadoopNodes );
                    if ( !masterNode.equals( wizard.getConfig().getGcNode() ) )
                    {
                        gcNodeCombo.setValue( wizard.getConfig().getGcNode() );
                    }
                    else
                    {
                        wizard.getConfig().setGcNode( null );
                    }
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
                    Agent gcNode = ( Agent ) event.getProperty().getValue();
                    wizard.getConfig().setGcNode( gcNode );
                    Config hadoopInfo =
                        ( Config ) hadoopClustersCombo.getValue();
                    List<Agent> hadoopNodes = filterAgents( hadoopInfo, zkClusters );
                    hadoopNodes.remove( gcNode );
                    masterNodeCombo.removeValueChangeListener( masterNodeComboChangeListener );
                    setComboDS( masterNodeCombo, hadoopNodes );
                    if ( !gcNode.equals( wizard.getConfig().getMasterNode() ) )
                    {
                        masterNodeCombo.setValue( wizard.getConfig().getMasterNode() );
                    }
                    else
                    {
                        wizard.getConfig().setMasterNode( null );
                    }
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
                    Agent monitor = ( Agent ) event.getProperty().getValue();
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

        instanceNameTxtFld.setValue( wizard.getConfig().getInstanceName() );
        instanceNameTxtFld.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                wizard.getConfig().setInstanceName( event.getProperty().getValue().toString().trim() );
            }
        } );

        passwordField.setValue( wizard.getConfig().getPassword() );
        passwordField.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                wizard.getConfig().setPassword( event.getProperty().getValue().toString().trim() );
            }
        } );

        confirmPass.setValue( wizard.getConfig().getConfirmPassword() );
        confirmPass.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                wizard.getConfig().setConfirmPassword( ( event.getProperty().getValue().toString().trim() ) );
            }
        } );

        //add value change handler
        tracersSelect.addValueChangeListener( new Property.ValueChangeListener()
        {

            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    Set<Agent> agentList = new HashSet( ( Collection ) event.getProperty().getValue() );
                    wizard.getConfig().setTracers( agentList );
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
                    Set<Agent> agentList = new HashSet( ( Collection ) event.getProperty().getValue() );
                    wizard.getConfig().setSlaves( agentList );
                }
            }
        } );

        Button next = new Button( "Next" );
        next.addStyleName( "default" );
        //check valid configuration
        next.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {

                if ( Strings.isNullOrEmpty( wizard.getConfig().getClusterName() ) )
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
                else if ( Strings.isNullOrEmpty( wizard.getConfig().getConfirmPassword() ) )
                {
                    show( "Please, confirm password" );
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
                else if ( !wizard.getConfig().getPassword().equals( wizard.getConfig().getConfirmPassword() ) )
                {
                    show( "Passwords does not match !!!" );
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
        masters.addComponent( hadoopClustersCombo );
        masters.addComponent( masterNodeCombo );
        masters.addComponent( gcNodeCombo );
        masters.addComponent( monitorNodeCombo );

        HorizontalLayout credentials = new HorizontalLayout();
        credentials.setMargin( new MarginInfo( true, false, false, false ) );
        credentials.setSpacing( true );
        credentials.addComponent( instanceNameTxtFld );
        credentials.addComponent( passwordField );
        credentials.addComponent( confirmPass );

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


    private void setComboDS( ComboBox target, List<Agent> hadoopNodes )
    {
        target.removeAllItems();
        target.setValue( null );
        for ( Agent agent : hadoopNodes )
        {
            target.addItem( agent );
            target.setItemCaption( agent, agent.getHostname() );
        }
    }


    private List<Agent> filterAgents( Config hadoopInfo,
        List<org.safehaus.subutai.api.zookeeper.Config> zkClusters )
    {

        List<Agent> filteredAgents = new ArrayList<>();
        org.safehaus.subutai.api.zookeeper.Config zkConfig = null;

        for ( org.safehaus.subutai.api.zookeeper.Config zkInfo : zkClusters )
        {
            if ( zkInfo.getClusterName().equals( hadoopInfo.getClusterName() ) )
            {
                zkConfig = zkInfo;
                break;
            }
        }

        if ( zkConfig != null )
        {
            filteredAgents.addAll( hadoopInfo.getAllNodes() );
            filteredAgents.retainAll( zkConfig.getNodes() );
        }

        return filteredAgents;
    }


    private void setTwinSelectDS( TwinColSelect target, List<Agent> hadoopNodes )
    {
        target.setValue( null );
        target.setContainerDataSource( new BeanItemContainer<>( Agent.class, hadoopNodes ) );
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }
}
