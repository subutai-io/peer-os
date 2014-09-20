/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hbase.ui.wizard;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;


/**
 * @author dilshat
 */
public class ConfigurationStep extends VerticalLayout
{

    private final ComboBox hadoopClusters;
    private final TwinColSelect select;


    public ConfigurationStep( final Wizard wizard )
    {

        setSizeFull();

        GridLayout content = new GridLayout( 1, 2 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );

        select = new TwinColSelect( "Nodes", new ArrayList<String>() );
        hadoopClusters = new ComboBox( "Hadoop cluster" );
        hadoopClusters.setImmediate( true );
        hadoopClusters.setTextInputAllowed( false );
        hadoopClusters.setRequired( true );
        hadoopClusters.setNullSelectionAllowed( false );

        List<HadoopClusterConfig> clusters = wizard.gethBaseUI().getHadoopManager().getClusters();
        if ( clusters.size() > 0 )
        {
            for ( HadoopClusterConfig config : clusters )
            {
                hadoopClusters.addItem( config );
                hadoopClusters.setItemCaption( config, config.getClusterName() );
            }
        }

        HadoopClusterConfig info =
                wizard.gethBaseUI().getHadoopManager().getCluster( wizard.getConfig().getClusterName() );

        if ( info != null )
        {
            hadoopClusters.setValue( info );
        }
        else if ( clusters.size() > 0 )
        {
            hadoopClusters.setValue( clusters.iterator().next() );
        }

        if ( hadoopClusters.getValue() != null )
        {
            HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) hadoopClusters.getValue();
            wizard.getConfig().setClusterName( hadoopInfo.getClusterName() );
            wizard.getConfig().setHadoopNameNode( hadoopInfo.getNameNode().getHostname() );
            Set<String> agentList = new HashSet<>();
            for ( Agent agent : hadoopInfo.getAllNodes() )
            {
                agentList.add( agent.getHostname() );
            }
            select.setContainerDataSource( new BeanItemContainer<>( String.class, agentList ) );
        }

        hadoopClusters.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) event.getProperty().getValue();
                    select.setValue( null );
                    Set<String> agentList = new HashSet<>();
                    for ( Agent agent : hadoopInfo.getAllNodes() )
                    {
                        agentList.add( agent.getHostname() );
                    }
                    select.setContainerDataSource( new BeanItemContainer<>( String.class, agentList ) );
                    wizard.getConfig().setClusterName( hadoopInfo.getClusterName() );
                    wizard.getConfig().setNodes( new HashSet<String>() );
                    wizard.getConfig().setHadoopNameNode( hadoopInfo.getNameNode().getHostname() );
                }
            }
        } );

        //        select.setItemCaptionPropertyId( "hostname" );
        select.setRows( 7 );
        select.setNullSelectionAllowed( false );
        select.setMultiSelect( true );
        select.setImmediate( true );
        select.setLeftColumnCaption( "Available Nodes" );
        select.setRightColumnCaption( "Selected Nodes" );
        select.setWidth( 100, Unit.PERCENTAGE );
        select.setRequired( true );
        if ( !CollectionUtil.isCollectionEmpty( wizard.getConfig().getNodes() ) )
        {
            select.setValue( wizard.getConfig().getNodes() );
        }
        select.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    Set<String> agentList = new HashSet( ( Collection ) event.getProperty().getValue() );
                    Set<String> hostnames = new HashSet<>();
                    for ( String agent : agentList )
                    {
                        hostnames.add( agent );
                    }

                    wizard.getConfig().setNodes( hostnames );
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
                Set<String> hostnames = new HashSet<>();
                for ( String agent : ( Set<String> ) select.getValue() )
                {
                    hostnames.add( agent );
                }

                wizard.getConfig().setNodes( hostnames );

                if ( Strings.isNullOrEmpty( wizard.getConfig().getClusterName() ) )
                {
                    show( "Please, select Hadoop cluster" );
                }
                else if ( CollectionUtil.isCollectionEmpty( wizard.getConfig().getNodes() ) )
                {
                    show( "Please, select target nodes" );
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

        content.addComponent( hadoopClusters );
        content.addComponent( select );
        content.addComponent( buttons );

        addComponent( layout );
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }
}
