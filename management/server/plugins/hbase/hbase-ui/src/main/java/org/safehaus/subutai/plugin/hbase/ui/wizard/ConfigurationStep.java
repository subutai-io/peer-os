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
import java.util.UUID;

import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;
import org.safehaus.subutai.plugin.hbase.api.SetupType;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.ComponentContainer;
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
    private final Hadoop hadoop;


    public ConfigurationStep( final Hadoop hadoop, final Wizard wizard )
    {

        this.hadoop = hadoop;
        setSizeFull();

        GridLayout content = new GridLayout( 1, 4 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );

        TextField nameTxt = new TextField( "Cluster name" );
        nameTxt.setId( "hbaseClusterName" );
        nameTxt.setRequired( true );
        nameTxt.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent e )
            {
                wizard.getConfig().setClusterName( e.getProperty().getValue().toString().trim() );
            }
        } );
        nameTxt.setValue( wizard.getConfig().getClusterName() );

        Button next = new Button( "Next" );
        next.setId( "HbaseNext" );
        next.addStyleName( "default" );
        next.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                nextClickHandler( wizard );
            }
        } );

        Button back = new Button( "Back" );
        back.setId( "HbaseBack" );
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

        content.addComponent( nameTxt );
        if ( wizard.getConfig().getSetupType() == SetupType.OVER_HADOOP )
        {
            addOverHadoopComponents( content, wizard.getConfig() );
        }
        //        else if ( wizard.getConfig().getSetupType() == SetupType.WITH_HADOOP )
        //        {
        //            addWithHadoopComponents( content, wizard.getConfig(), wizard.getHadoopConfig() );
        //        }
        content.addComponent( buttons );

        setContent( layout );
    }


    private void addOverHadoopComponents( ComponentContainer parent, final HBaseClusterConfig config )
    {
        final ComboBox hadoopClustersCombo = new ComboBox( "Hadoop cluster" );
        final ComboBox masterNodeCombo = new ComboBox( "Master node" );
        final TwinColSelect regionServers = new TwinColSelect( "Region Servers", new ArrayList<UUID>() );
        final TwinColSelect quorumPeers = new TwinColSelect( "Quroum Peers", new ArrayList<UUID>() );
        final TwinColSelect backUpMasters = new TwinColSelect( "Backup Masters", new ArrayList<UUID>() );

        hadoopClustersCombo.setId( "HbaseConfHadoopCluster" );
        masterNodeCombo.setId( "HbaseMasters" );
        regionServers.setId( "HbaseRegions" );
        quorumPeers.setId( "HbaseQuorums" );
        backUpMasters.setId( "HbaseBackupMasters" );

        masterNodeCombo.setImmediate( true );
        masterNodeCombo.setTextInputAllowed( false );
        masterNodeCombo.setRequired( true );
        masterNodeCombo.setNullSelectionAllowed( false );

        hadoopClustersCombo.setImmediate( true );
        hadoopClustersCombo.setTextInputAllowed( false );
        hadoopClustersCombo.setRequired( true );
        hadoopClustersCombo.setNullSelectionAllowed( false );

        regionServers.setItemCaptionPropertyId( "hostname" );
        regionServers.setRows( 7 );
        regionServers.setMultiSelect( true );
        regionServers.setImmediate( true );
        regionServers.setLeftColumnCaption( "Available Nodes" );
        regionServers.setRightColumnCaption( "Selected Nodes" );
        regionServers.setWidth( 100, Unit.PERCENTAGE );
        regionServers.setRequired( true );


        quorumPeers.setItemCaptionPropertyId( "hostname" );
        quorumPeers.setRows( 7 );
        quorumPeers.setMultiSelect( true );
        quorumPeers.setImmediate( true );
        quorumPeers.setLeftColumnCaption( "Available Nodes" );
        quorumPeers.setRightColumnCaption( "Selected Nodes" );
        quorumPeers.setWidth( 100, Unit.PERCENTAGE );
        quorumPeers.setRequired( true );


        backUpMasters.setItemCaptionPropertyId( "hostname" );
        backUpMasters.setRows( 7 );
        backUpMasters.setMultiSelect( true );
        backUpMasters.setImmediate( true );
        backUpMasters.setLeftColumnCaption( "Available Nodes" );
        backUpMasters.setRightColumnCaption( "Selected Nodes" );
        backUpMasters.setWidth( 100, Unit.PERCENTAGE );
        backUpMasters.setRequired( true );


        List<HadoopClusterConfig> clusters = hadoop.getClusters();

        if ( clusters.size() > 0 )
        {
            for ( HadoopClusterConfig hadoopClusterInfo : clusters )
            {
                hadoopClustersCombo.addItem( hadoopClusterInfo );
                hadoopClustersCombo.setItemCaption( hadoopClusterInfo, hadoopClusterInfo.getClusterName() );
            }
        }

        if ( Strings.isNullOrEmpty( config.getClusterName() ) )
        {
            if ( clusters.size() > 0 )
            {
                hadoopClustersCombo.setValue( clusters.iterator().next() );
            }
        }
        else
        {
            HadoopClusterConfig info = hadoop.getCluster( config.getClusterName() );
            if ( info != null )
            //restore cluster
            {
                hadoopClustersCombo.setValue( info );
            }
            else if ( clusters.size() > 0 )
            {
                hadoopClustersCombo.setValue( clusters.iterator().next() );
            }
        }

        if ( hadoopClustersCombo.getValue() != null )
        {
            HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) hadoopClustersCombo.getValue();
            config.setHadoopClusterName( hadoopInfo.getClusterName() );
            config.setHadoopNameNode( hadoopInfo.getNameNode().toString() );

            /** fill all tables  */
            regionServers.setContainerDataSource( new BeanItemContainer<>( UUID.class, hadoopInfo.getAllNodes() ) );
            quorumPeers.setContainerDataSource( new BeanItemContainer<>( UUID.class, hadoopInfo.getAllNodes() ) );
            backUpMasters.setContainerDataSource( new BeanItemContainer<>( UUID.class, hadoopInfo.getAllNodes() ) );
            for ( UUID agent : hadoopInfo.getAllNodes() )
            {
                masterNodeCombo.addItem( agent );
                masterNodeCombo.setItemCaption( agent, agent.toString() );
            }
        }

        hadoopClustersCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) event.getProperty().getValue();
                    regionServers.setValue( null );
                    regionServers
                            .setContainerDataSource( new BeanItemContainer<>( UUID.class, hadoopInfo.getAllNodes() ) );

                    quorumPeers.setValue( null );
                    quorumPeers
                            .setContainerDataSource( new BeanItemContainer<>( UUID.class, hadoopInfo.getAllNodes() ) );

                    backUpMasters.setValue( null );
                    backUpMasters
                            .setContainerDataSource( new BeanItemContainer<>( UUID.class, hadoopInfo.getAllNodes() ) );

                    masterNodeCombo.setValue( null );
                    masterNodeCombo.removeAllItems();
                    for ( UUID agent : hadoopInfo.getAllNodes() )
                    {
                        masterNodeCombo.addItem( agent );
                        masterNodeCombo.setItemCaption( agent, agent.toString() );
                    }
                    config.setHadoopClusterName( hadoopInfo.getClusterName() );
                    config.setRegionServers( new HashSet<UUID>() );
                    config.setQuorumPeers( new HashSet<UUID>() );
                    config.setBackupMasters( new HashSet<UUID>() );
                    config.setHbaseMaster( null );
                }
            }
        } );

        masterNodeCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    UUID master = ( UUID ) event.getProperty().getValue();
                    config.setHbaseMaster( master );


                    HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) hadoopClustersCombo.getValue();
                    if ( config.getBackupMasters() != null && !config.getBackupMasters().isEmpty() )
                    {
                        config.getBackupMasters().remove( master );
                    }
                    List<UUID> hadoopNodes = hadoopInfo.getAllNodes();
                    hadoopNodes.remove( master );

                    /** fill region servers table */
                    regionServers.getContainerDataSource().removeAllItems();
                    for ( UUID agent : hadoopNodes )
                    {
                        regionServers.getContainerDataSource().addItem( agent );
                    }

                    Collection ls = regionServers.getListeners( Property.ValueChangeListener.class );
                    Property.ValueChangeListener h =
                            ls.isEmpty() ? null : ( Property.ValueChangeListener ) ls.iterator().next();
                    if ( h != null )
                    {
                        regionServers.removeValueChangeListener( h );
                    }
                    regionServers.setValue( config.getRegionServers() );
                    if ( h != null )
                    {
                        regionServers.addValueChangeListener( h );
                    }

                    /** fill quorum peers servers table */
                    quorumPeers.getContainerDataSource().removeAllItems();
                    for ( UUID agent : hadoopNodes )
                    {
                        quorumPeers.getContainerDataSource().addItem( agent );
                    }

                    ls = quorumPeers.getListeners( Property.ValueChangeListener.class );
                    h = ls.isEmpty() ? null : ( Property.ValueChangeListener ) ls.iterator().next();
                    if ( h != null )
                    {
                        quorumPeers.removeValueChangeListener( h );
                    }
                    quorumPeers.setValue( config.getQuorumPeers() );
                    if ( h != null )
                    {
                        quorumPeers.addValueChangeListener( h );
                    }

                    /** fill back up master servers table */
                    backUpMasters.getContainerDataSource().removeAllItems();
                    for ( UUID agent : hadoopNodes )
                    {
                        backUpMasters.getContainerDataSource().addItem( agent );
                    }

                    ls = backUpMasters.getListeners( Property.ValueChangeListener.class );
                    h = ls.isEmpty() ? null : ( Property.ValueChangeListener ) ls.iterator().next();
                    if ( h != null )
                    {
                        backUpMasters.removeValueChangeListener( h );
                    }
                    backUpMasters.setValue( config.getBackupMasters() );
                    if ( h != null )
                    {
                        backUpMasters.addValueChangeListener( h );
                    }
                }
            }
        } );

        if ( config.getHbaseMaster() != null )
        {
            masterNodeCombo.setValue( config.getHbaseMaster() );
        }

        if ( !CollectionUtil.isCollectionEmpty( config.getRegionServers() ) )
        {
            regionServers.setValue( config.getRegionServers() );
        }

        if ( !CollectionUtil.isCollectionEmpty( config.getQuorumPeers() ) )
        {
            regionServers.setValue( config.getQuorumPeers() );
        }

        if ( !CollectionUtil.isCollectionEmpty( config.getBackupMasters() ) )
        {
            regionServers.setValue( config.getBackupMasters() );
        }

        regionServers.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    Set<UUID> agentList = new HashSet<>( ( Collection<UUID> ) event.getProperty().getValue() );
                    config.setRegionServers( agentList );
                }
            }
        } );

        quorumPeers.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    Set<UUID> agentList = new HashSet<>( ( Collection<UUID> ) event.getProperty().getValue() );
                    config.setQuorumPeers( agentList );
                }
            }
        } );


        backUpMasters.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    Set<UUID> agentList = new HashSet<>( ( Collection<UUID> ) event.getProperty().getValue() );
                    config.setBackupMasters( agentList );
                }
            }
        } );

        parent.addComponent( hadoopClustersCombo );
        parent.addComponent( masterNodeCombo );
        parent.addComponent( regionServers );
        parent.addComponent( quorumPeers );
        parent.addComponent( backUpMasters );
    }


    private void nextClickHandler( Wizard wizard )
    {
        HBaseClusterConfig config = wizard.getConfig();
        if ( config.getClusterName() == null || config.getClusterName().isEmpty() )
        {
            show( "Enter cluster name" );
            return;
        }

        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            if ( Strings.isNullOrEmpty( config.getHadoopClusterName() ) )
            {
                show( "Please, select Hadoop cluster" );
            }
            else if ( config.getHbaseMaster() == null )
            {
                show( "Please, select master node" );
            }
            else if ( CollectionUtil.isCollectionEmpty( config.getRegionServers() ) )
            {
                show( "Please, select nodes for region servers" );
            }
            else if ( CollectionUtil.isCollectionEmpty( config.getQuorumPeers() ) )
            {
                show( "Please, select nodes for quorum peers" );
            }
            else if ( CollectionUtil.isCollectionEmpty( config.getBackupMasters() ) )
            {
                show( "Please, select for back up masters" );
            }
            else
            {
                wizard.next();
            }
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP )
        {
            HadoopClusterConfig hc = wizard.getHadoopConfig();
            if ( hc.getClusterName() == null || hc.getClusterName().isEmpty() )
            {
                show( "Enter Hadoop cluster name" );
            }
            else if ( hc.getCountOfSlaveNodes() <= 0 )
            {
                show( "Invalid number of Hadoop slave nodes" );
            }
            else if ( hc.getReplicationFactor() <= 0 )
            {
                show( "Invalid replication factor" );
            }
            else if ( hc.getDomainName() == null || hc.getDomainName().isEmpty() )
            {
                show( "Enter Hadoop domain name" );
            }
            else
            {
                wizard.next();
            }
        }
        else
        {
            show( "Installation type not supported" );
        }
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }
}
