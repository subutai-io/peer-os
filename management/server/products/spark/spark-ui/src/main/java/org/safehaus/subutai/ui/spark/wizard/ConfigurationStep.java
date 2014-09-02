/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.spark.wizard;


import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.hadoop.Config;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.ui.spark.SparkUI;

import java.util.*;


/**
 * @author dilshat
 */
public class ConfigurationStep extends Panel
{

    private final TwinColSelect slaveNodesSelect;
    private final ComboBox masterNodeCombo;


    public ConfigurationStep( final Wizard wizard )
    {

        setSizeFull();

        GridLayout content = new GridLayout( 1, 4 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );

        ComboBox hadoopClustersCombo = new ComboBox( "Hadoop cluster" );
        masterNodeCombo = new ComboBox( "Master node" );
        slaveNodesSelect = new TwinColSelect( "Slave nodes", new ArrayList<Agent>() );

        masterNodeCombo.setImmediate( true );
        masterNodeCombo.setTextInputAllowed( false );
        masterNodeCombo.setRequired( true );
        masterNodeCombo.setNullSelectionAllowed( false );

        hadoopClustersCombo.setImmediate( true );
        hadoopClustersCombo.setTextInputAllowed( false );
        hadoopClustersCombo.setRequired( true );
        hadoopClustersCombo.setNullSelectionAllowed( false );

        slaveNodesSelect.setItemCaptionPropertyId( "hostname" );
        slaveNodesSelect.setRows( 7 );
        slaveNodesSelect.setMultiSelect( true );
        slaveNodesSelect.setImmediate( true );
        slaveNodesSelect.setLeftColumnCaption( "Available Nodes" );
        slaveNodesSelect.setRightColumnCaption( "Selected Nodes" );
        slaveNodesSelect.setWidth( 100, Unit.PERCENTAGE );
        slaveNodesSelect.setRequired( true );

        List<Config> clusters = SparkUI.getHadoopManager().getClusters();

        if ( clusters.size() > 0 )
        {
            for ( Config hadoopClusterInfo : clusters )
            {
                hadoopClustersCombo.addItem( hadoopClusterInfo );
                hadoopClustersCombo.setItemCaption( hadoopClusterInfo,
                    hadoopClusterInfo.getClusterName() );
            }
        }

        Config info = SparkUI.getHadoopManager().getCluster( wizard.getConfig().getClusterName() );

        if ( info != null )
        {
            hadoopClustersCombo.setValue( info );
        }
        else if ( clusters.size() > 0 )
        {
            hadoopClustersCombo.setValue( clusters.iterator().next() );
        }

        if ( hadoopClustersCombo.getValue() != null )
        {
            Config hadoopInfo = ( Config ) hadoopClustersCombo.getValue();
            wizard.getConfig().setClusterName( hadoopInfo.getClusterName() );
            slaveNodesSelect.setContainerDataSource(
                new BeanItemContainer<>(
                    Agent.class, hadoopInfo.getAllNodes() )
            );
            for ( Agent agent : hadoopInfo.getAllNodes() )
            {
                masterNodeCombo.addItem( agent );
                masterNodeCombo.setItemCaption( agent, agent.getHostname() );
            }
        }

        hadoopClustersCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    Config hadoopInfo = ( Config ) event.getProperty().getValue();
                    slaveNodesSelect.setValue( null );
                    slaveNodesSelect.setContainerDataSource(
                        new BeanItemContainer<>(
                            Agent.class, hadoopInfo.getAllNodes() )
                    );
                    masterNodeCombo.setValue( null );
                    masterNodeCombo.removeAllItems();
                    for ( Agent agent : hadoopInfo.getAllNodes() )
                    {
                        masterNodeCombo.addItem( agent );
                        masterNodeCombo.setItemCaption( agent, agent.getHostname() );
                    }
                    wizard.getConfig().setClusterName( hadoopInfo.getClusterName() );
                    wizard.getConfig().setSlaveNodes( new HashSet<Agent>() );
                    wizard.getConfig().setMasterNode( null );
                }
            }
        } );

        if ( wizard.getConfig().getMasterNode() != null )
        {
            masterNodeCombo.setValue( wizard.getConfig().getMasterNode() );
        }

        masterNodeCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    Agent master = ( Agent ) event.getProperty().getValue();
                    wizard.getConfig().setMasterNode( master );
                }
            }
        } );

        if ( !CollectionUtil.isCollectionEmpty( wizard.getConfig().getSlaveNodes() ) )
        {
            slaveNodesSelect.setValue( wizard.getConfig().getSlaveNodes() );
        }
        slaveNodesSelect.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    Set<Agent> agentList = new HashSet( ( Collection ) event.getProperty().getValue() );
                    wizard.getConfig().setSlaveNodes( agentList );
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
                    show( "Please, select Hadoop cluster" );
                }
                else if ( wizard.getConfig().getMasterNode() == null )
                {
                    show( "Please, select master node" );
                }
                else if ( CollectionUtil.isCollectionEmpty( wizard.getConfig().getSlaveNodes() ) )
                {
                    show( "Please, select slave nodes" );
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

        content.addComponent( hadoopClustersCombo );
        content.addComponent( masterNodeCombo );
        content.addComponent( slaveNodesSelect );
        content.addComponent( buttons );

        setContent( layout );

    }


    private void show( String notification )
    {
        Notification.show( notification );
    }

}
