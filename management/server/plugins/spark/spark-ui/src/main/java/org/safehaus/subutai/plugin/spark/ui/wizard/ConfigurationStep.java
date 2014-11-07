package org.safehaus.subutai.plugin.spark.ui.wizard;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.spark.api.SetupType;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
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
    private final EnvironmentManager environmentManager;
    private Environment hadoopEnvironment;


    public ConfigurationStep( final Hadoop hadoop, final EnvironmentManager environmentManager, final Wizard wizard )
    {

        this.hadoop = hadoop;
        this.environmentManager = environmentManager;
        setSizeFull();

        GridLayout content = new GridLayout( 1, 4 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );

        TextField nameTxt = new TextField( "Cluster name" );
        nameTxt.setId( "sparkClusterName" );
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
        next.setId( "sparkNext" );
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
        back.setId( "sparkBack" );
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
        else if ( wizard.getConfig().getSetupType() == SetupType.WITH_HADOOP )
        {
            addWithHadoopComponents( content, wizard.getConfig(), wizard.getHadoopConfig() );
        }
        content.addComponent( buttons );

        setContent( layout );
    }


    private void addOverHadoopComponents( ComponentContainer parent, final SparkClusterConfig config )
    {
        final ComboBox hadoopClustersCombo = new ComboBox( "Hadoop cluster" );
        final ComboBox masterNodeCombo = new ComboBox( "Master node" );
        final TwinColSelect slaveNodesSelect = new TwinColSelect( "Slave nodes", new ArrayList<Agent>() );

        hadoopClustersCombo.setId( "sparkHadoopCluster" );
        masterNodeCombo.setId( "sparkMasterNode" );
        slaveNodesSelect.setId( "sparkSlaveNodes" );

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

        List<HadoopClusterConfig> clusters = hadoop.getClusters();

        if ( !clusters.isEmpty() )
        {
            for ( HadoopClusterConfig hadoopClusterInfo : clusters )
            {
                hadoopClustersCombo.addItem( hadoopClusterInfo );
                hadoopClustersCombo.setItemCaption( hadoopClusterInfo, hadoopClusterInfo.getClusterName() );
            }
        }

        if ( Strings.isNullOrEmpty( config.getClusterName() ) )
        {
            if ( !clusters.isEmpty() )
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
            else if ( !clusters.isEmpty() )
            {
                hadoopClustersCombo.setValue( clusters.iterator().next() );
            }
        }


        if ( hadoopClustersCombo.getValue() != null )
        {
            HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) hadoopClustersCombo.getValue();
            config.setHadoopClusterName( hadoopInfo.getClusterName() );
            hadoopEnvironment = environmentManager.getEnvironmentByUUID( hadoopInfo.getEnvironmentId() );
            Set<ContainerHost> hadoopNodes =
                    hadoopEnvironment.getHostsByIds( Sets.newHashSet( hadoopInfo.getAllNodes() ) );
            slaveNodesSelect.setContainerDataSource( new BeanItemContainer<>( ContainerHost.class, hadoopNodes ) );
            for ( ContainerHost hadoopNode : hadoopNodes )
            {
                masterNodeCombo.addItem( hadoopNode );
                masterNodeCombo.setItemCaption( hadoopNode, hadoopNode.getHostname() );
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
                    hadoopEnvironment = environmentManager.getEnvironmentByUUID( hadoopInfo.getEnvironmentId() );
                    Set<ContainerHost> hadoopNodes =
                            hadoopEnvironment.getHostsByIds( Sets.newHashSet( hadoopInfo.getAllNodes() ) );
                    slaveNodesSelect.setValue( null );
                    slaveNodesSelect
                            .setContainerDataSource( new BeanItemContainer<>( ContainerHost.class, hadoopNodes ) );
                    masterNodeCombo.setValue( null );
                    masterNodeCombo.removeAllItems();
                    for ( ContainerHost hadoopNode : hadoopNodes )
                    {
                        masterNodeCombo.addItem( hadoopNode );
                        masterNodeCombo.setItemCaption( hadoopNode, hadoopNode.getHostname() );
                    }
                    config.setHadoopClusterName( hadoopInfo.getClusterName() );
                    config.getSlaveIds().clear();
                    config.setMasterNodeId( null );
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
                    config.setMasterNodeId( master.getId() );

                    //fill slave nodes without newly selected master node
                    HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) hadoopClustersCombo.getValue();
                    config.getSlaveIds().remove( master.getId() );

                    hadoopEnvironment = environmentManager.getEnvironmentByUUID( hadoopInfo.getEnvironmentId() );
                    Set<ContainerHost> hadoopNodes =
                            hadoopEnvironment.getHostsByIds( Sets.newHashSet( hadoopInfo.getAllNodes() ) );
                    hadoopNodes.remove( master );
                    slaveNodesSelect.getContainerDataSource().removeAllItems();
                    for ( ContainerHost hadoopNode : hadoopNodes )
                    {
                        slaveNodesSelect.getContainerDataSource().addItem( hadoopNode );
                    }

                    Collection ls = slaveNodesSelect.getListeners( Property.ValueChangeListener.class );
                    Property.ValueChangeListener h =
                            ls.isEmpty() ? null : ( Property.ValueChangeListener ) ls.iterator().next();
                    if ( h != null )
                    {
                        slaveNodesSelect.removeValueChangeListener( h );
                    }

                    slaveNodesSelect.setValue( hadoopEnvironment.getHostsByIds( config.getSlaveIds() ) );
                    if ( h != null )
                    {
                        slaveNodesSelect.addValueChangeListener( h );
                    }
                }
            }
        } );

        if ( config.getMasterNodeId() != null )
        {
            masterNodeCombo.setValue( hadoopEnvironment.getContainerHostByUUID( config.getMasterNodeId() ) );
        }

        if ( !CollectionUtil.isCollectionEmpty( config.getSlaveIds() ) )
        {
            slaveNodesSelect.setValue( hadoopEnvironment.getHostsByIds( config.getSlaveIds() ) );
        }

        slaveNodesSelect.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    Set<ContainerHost> nodes = ( Set<ContainerHost> ) event.getProperty().getValue();
                    Set<UUID> slaveIds = Sets.newHashSet();
                    for ( ContainerHost node : nodes )
                    {
                        slaveIds.add( node.getId() );
                    }
                    config.getSlaveIds().clear();
                    config.getSlaveIds().addAll( slaveIds );
                }
            }
        } );

        parent.addComponent( hadoopClustersCombo );
        parent.addComponent( masterNodeCombo );
        parent.addComponent( slaveNodesSelect );
    }


    private void addWithHadoopComponents( ComponentContainer parent, final SparkClusterConfig config,
                                          final HadoopClusterConfig hadoopConfig )
    {

        Collection<Integer> col = Arrays.asList( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 );

        final TextField txtHadoopClusterName = new TextField( "Hadoop cluster name" );
        txtHadoopClusterName.setRequired( true );
        txtHadoopClusterName.setMaxLength( 20 );
        if ( hadoopConfig.getClusterName() != null )
        {
            txtHadoopClusterName.setValue( hadoopConfig.getClusterName() );
        }
        txtHadoopClusterName.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                String name = event.getProperty().getValue().toString().trim();
                config.setHadoopClusterName( name );
                hadoopConfig.setClusterName( name );
            }
        } );

        ComboBox cmbSlaveNodes = new ComboBox( "Number of Hadoop slave nodes", col );
        cmbSlaveNodes.setImmediate( true );
        cmbSlaveNodes.setTextInputAllowed( false );
        cmbSlaveNodes.setNullSelectionAllowed( false );
        cmbSlaveNodes.setValue( hadoopConfig.getCountOfSlaveNodes() );
        cmbSlaveNodes.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                hadoopConfig.setCountOfSlaveNodes( ( Integer ) event.getProperty().getValue() );
            }
        } );

        ComboBox cmbReplFactor = new ComboBox( "Replication factor for Hadoop slave nodes", col );
        cmbReplFactor.setImmediate( true );
        cmbReplFactor.setTextInputAllowed( false );
        cmbReplFactor.setNullSelectionAllowed( false );
        cmbReplFactor.setValue( hadoopConfig.getReplicationFactor() );
        cmbReplFactor.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                hadoopConfig.setReplicationFactor( ( Integer ) event.getProperty().getValue() );
            }
        } );

        TextField txtHadoopDomain = new TextField( "Hadoop cluster domain name" );
        txtHadoopDomain.setInputPrompt( hadoopConfig.getDomainName() );
        txtHadoopDomain.setValue( hadoopConfig.getDomainName() );
        txtHadoopDomain.setMaxLength( 20 );
        txtHadoopDomain.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                String val = event.getProperty().getValue().toString().trim();
                if ( !val.isEmpty() )
                {
                    hadoopConfig.setDomainName( val );
                }
            }
        } );

        parent.addComponent( new Label( "Hadoop settings" ) );
        parent.addComponent( txtHadoopClusterName );
        parent.addComponent( cmbSlaveNodes );
        parent.addComponent( cmbReplFactor );
        parent.addComponent( txtHadoopDomain );
    }


    private void nextClickHandler( Wizard wizard )
    {
        SparkClusterConfig config = wizard.getConfig();
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
            else if ( config.getMasterNodeId() == null )
            {
                show( "Please, select master node" );
            }
            else if ( CollectionUtil.isCollectionEmpty( config.getSlaveIds() ) )
            {
                show( "Please, select slave node(s)" );
            }
            else
            {
                wizard.next();
            }
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP )
        {
            HadoopClusterConfig hc = wizard.getHadoopConfig();
            if ( Strings.isNullOrEmpty( hc.getClusterName() ) )
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
            else if ( Strings.isNullOrEmpty( hc.getDomainName() ) )
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
