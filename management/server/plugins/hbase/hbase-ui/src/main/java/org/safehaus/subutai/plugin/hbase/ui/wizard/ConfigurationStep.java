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

import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
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
    private final Wizard wizard;


    public ConfigurationStep( final Hadoop hadoop, final Wizard wizard )
    {

        this.hadoop = hadoop;
        this.wizard = wizard;

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


    private Set<ContainerHost> getHadoopContainerHosts( HadoopClusterConfig hadoopInfo )
    {
        Environment hadoopEnvironment =
                wizard.getEnvironmentManager().getEnvironment( hadoopInfo.getEnvironmentId().toString() );
        Set<ContainerHost> hadoopHosts = new HashSet<>();
        for ( ContainerHost host : hadoopEnvironment.getContainers() )
        {
            if ( host.getNodeGroupName().toLowerCase().contains( hadoopInfo.getProductName().toLowerCase() ) )
            {
                if ( hadoopInfo.getAllNodes().contains( host.getId() ) )
                {
                    hadoopHosts.add( host );
                }
            }
        }
        return hadoopHosts;
    }


    private void addOverHadoopComponents( ComponentContainer parent, final HBaseClusterConfig config )
    {
        final ComboBox hadoopClustersCombo = new ComboBox( "Hadoop cluster" );
        final ComboBox masterNodeCombo = new ComboBox( "Master node" );
        final TwinColSelect regionServers = new TwinColSelect( "Region Servers", new ArrayList<ContainerHost>() );
        final TwinColSelect quorumPeers = new TwinColSelect( "Quroum Peers", new ArrayList<ContainerHost>() );
        final TwinColSelect backUpMasters = new TwinColSelect( "Backup Masters", new ArrayList<ContainerHost>() );

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


            Set<ContainerHost> hadoopHosts = getHadoopContainerHosts( hadoopInfo );
            config.setEnvironmentId( hadoopInfo.getEnvironmentId() );
            regionServers.setContainerDataSource( new BeanItemContainer<>( ContainerHost.class, hadoopHosts ) );
            quorumPeers.setContainerDataSource( new BeanItemContainer<>( ContainerHost.class, hadoopHosts ) );
            backUpMasters.setContainerDataSource( new BeanItemContainer<>( ContainerHost.class, hadoopHosts ) );
            for ( ContainerHost host : hadoopHosts )
            {
                masterNodeCombo.addItem( host );
                masterNodeCombo.setItemCaption( host, host.getHostname() );
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
                    Environment environment =
                            wizard.getEnvironmentManager().getEnvironment( hadoopInfo.getEnvironmentId().toString() );

                    Set<ContainerHost> hadoopHosts = getHadoopContainerHosts( hadoopInfo );
                    regionServers.setValue( null );
                    regionServers.setContainerDataSource( new BeanItemContainer<>( ContainerHost.class, hadoopHosts ) );

                    quorumPeers.setValue( null );
                    quorumPeers.setContainerDataSource( new BeanItemContainer<>( ContainerHost.class, hadoopHosts ) );

                    backUpMasters.setValue( null );
                    backUpMasters.setContainerDataSource( new BeanItemContainer<>( ContainerHost.class, hadoopHosts ) );

                    masterNodeCombo.setValue( null );
                    masterNodeCombo.removeAllItems();
                    for ( ContainerHost host : hadoopHosts )
                    {
                        masterNodeCombo.addItem( host );
                        masterNodeCombo.setItemCaption( host, host.getHostname() );
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
                    ContainerHost master = ( ContainerHost ) event.getProperty().getValue();
                    config.setHbaseMaster( master.getId() );


                    HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) hadoopClustersCombo.getValue();
                    if ( config.getBackupMasters() != null && !config.getBackupMasters().isEmpty() )
                    {
                        config.getBackupMasters().remove( master );
                    }
                    //                    List<UUID> hadoopNodes = hadoopInfo.getAllNodes();
                    Set<ContainerHost> hadoopHosts = getHadoopContainerHosts( hadoopInfo );
                    hadoopHosts.remove( master );

                    /** fill region servers table */
                    regionServers.getContainerDataSource().removeAllItems();
                    for ( ContainerHost host : hadoopHosts )
                    {
                        regionServers.getContainerDataSource().addItem( host );
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
                    for ( ContainerHost host : hadoopHosts )
                    {
                        quorumPeers.getContainerDataSource().addItem( host );
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
                    for ( ContainerHost agent : hadoopHosts )
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
                    Set<ContainerHost> hosts =
                            new HashSet<>( ( Collection<ContainerHost> ) event.getProperty().getValue() );
                    Set<UUID> hostIds = new HashSet<UUID>();
                    for ( ContainerHost host : hosts )
                    {
                        hostIds.add( host.getId() );
                    }
                    config.setRegionServers( hostIds );
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
                    Set<ContainerHost> hosts =
                            new HashSet<>( ( Collection<ContainerHost> ) event.getProperty().getValue() );
                    Set<UUID> hostIds = new HashSet<UUID>();
                    for ( ContainerHost host : hosts )
                    {
                        hostIds.add( host.getId() );
                    }
                    config.setQuorumPeers( hostIds );
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
                    Set<ContainerHost> hosts =
                            new HashSet<>( ( Collection<ContainerHost> ) event.getProperty().getValue() );
                    Set<UUID> hostIds = new HashSet<UUID>();
                    for ( ContainerHost host : hosts )
                    {
                        hostIds.add( host.getId() );
                    }
                    config.setBackupMasters( hostIds );
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
