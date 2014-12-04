package org.safehaus.subutai.plugin.presto.ui.wizard;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.api.SetupType;

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

//import org.safehaus.subutai.common.protocol.Agent;


public class ConfigurationStep extends Panel
{

    private final Hadoop hadoop;
    Property.ValueChangeListener coordinatorComboChangeListener;
    Property.ValueChangeListener workersSelectChangeListener;
    private ComboBox hadoopClustersCombo;
    private TwinColSelect workersSelect;
    private ComboBox coordinatorNodeCombo;
    private Environment hadoopEnvironment;
    private final EnvironmentManager environmentManager;


    public ConfigurationStep( final Hadoop hadoop, final Wizard wizard, final EnvironmentManager environmentManager )
    {

        this.hadoop = hadoop;
        this.environmentManager = environmentManager;
        setSizeFull();

        GridLayout content = new GridLayout( 1, 4 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );

        TextField nameTxt = new TextField( "Cluster name" );
        nameTxt.setId( "PrestoClusterName" );
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
        next.setId( "PresConfNext" );
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
        back.setId( "PresConfBack" );
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
        PrestoClusterConfig config = wizard.getConfig();
        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            addOverHadoopComponents( content, config );
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP )
        {
            addWithHadoopComponents( content, config, wizard.getHadoopConfig() );
        }

        content.addComponent( buttons );

        setContent( layout );
    }


    private void addOverHadoopComponents( ComponentContainer parent, final PrestoClusterConfig config )
    {

        hadoopClustersCombo = new ComboBox( "Hadoop cluster" );
        hadoopClustersCombo.setId( "PresHadoopClusterCb" );
        coordinatorNodeCombo = new ComboBox( "Coordinator" );
        coordinatorNodeCombo.setId( "PresCoordinatorCb" );
        workersSelect = new TwinColSelect( "Workers", new ArrayList<ContainerHost>() );
        workersSelect.setId( "PresSelect" );

        coordinatorNodeCombo.setImmediate( true );
        coordinatorNodeCombo.setTextInputAllowed( false );
        coordinatorNodeCombo.setRequired( true );
        coordinatorNodeCombo.setNullSelectionAllowed( false );

        hadoopClustersCombo.setImmediate( true );
        hadoopClustersCombo.setTextInputAllowed( false );
        hadoopClustersCombo.setRequired( true );
        hadoopClustersCombo.setNullSelectionAllowed( false );

        workersSelect.setItemCaptionPropertyId( "hostname" );
        workersSelect.setRows( 7 );
        workersSelect.setMultiSelect( true );
        workersSelect.setImmediate( true );
        workersSelect.setLeftColumnCaption( "Available Nodes" );
        workersSelect.setRightColumnCaption( "Selected Nodes" );
        workersSelect.setWidth( 100, Unit.PERCENTAGE );
        workersSelect.setRequired( true );

        List<HadoopClusterConfig> clusters = hadoop.getClusters();

        //populate hadoop clusters combo
        if ( !clusters.isEmpty() )
        {
            for ( HadoopClusterConfig hadoopClusterInfo : clusters )
            {
                hadoopClustersCombo.addItem( hadoopClusterInfo );
                hadoopClustersCombo.setItemCaption( hadoopClusterInfo, hadoopClusterInfo.getClusterName() );
            }
        }

        if ( Strings.isNullOrEmpty( config.getHadoopClusterName() ) )
        {
            if ( !clusters.isEmpty() )
            {
                hadoopClustersCombo.setValue( clusters.iterator().next() );
            }
        }
        else
        {
            HadoopClusterConfig info = hadoop.getCluster( config.getHadoopClusterName() );
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

        //populate selection controls
        if ( hadoopClustersCombo.getValue() != null )
        {
            HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) hadoopClustersCombo.getValue();
            config.setHadoopClusterName( hadoopInfo.getClusterName() );
            hadoopEnvironment = environmentManager.getEnvironmentByUUID( hadoopInfo.getEnvironmentId() );
            Set<ContainerHost> hadoopNodes =
                    hadoopEnvironment.getContainerHostsByIds( Sets.newHashSet( hadoopInfo.getAllNodes() ) );
            workersSelect.setContainerDataSource( new BeanItemContainer<>( ContainerHost.class, hadoopNodes ) );
            for ( ContainerHost hadoopNode : hadoopNodes )
            {
                coordinatorNodeCombo.addItem( hadoopNode );
                coordinatorNodeCombo.setItemCaption( hadoopNode, hadoopNode.getHostname() );
            }
        }
        //restore coordinator
        if ( config.getCoordinatorNode() != null )
        {
            coordinatorNodeCombo.setValue( config.getCoordinatorNode() );
            workersSelect.getContainerDataSource().removeItem( config.getCoordinatorNode() );
        }

        //restore workers
        if ( !CollectionUtil.isCollectionEmpty( config.getWorkers() ) )
        {
            workersSelect.setValue( config.getWorkers() );
            for ( UUID worker : config.getWorkers() )
            {
                coordinatorNodeCombo.removeItem( worker );
            }
        }

        //hadoop cluster selection change listener
        hadoopClustersCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) event.getProperty().getValue();
                    config.setHadoopClusterName( hadoopInfo.getClusterName() );
                    hadoopEnvironment = environmentManager.getEnvironmentByUUID( hadoopInfo.getEnvironmentId() );
                    Set<ContainerHost> hadoopNodes =
                            hadoopEnvironment.getContainerHostsByIds( Sets.newHashSet( hadoopInfo.getAllNodes() ) );
                    workersSelect.setValue( null );
                    workersSelect.setContainerDataSource( new BeanItemContainer<>( ContainerHost.class, hadoopNodes ) );
                    coordinatorNodeCombo.setValue( null );
                    coordinatorNodeCombo.removeAllItems();
                    for ( ContainerHost hadoopNode : hadoopNodes )
                    {
                        coordinatorNodeCombo.addItem( hadoopNode );
                        coordinatorNodeCombo.setItemCaption( hadoopNode, hadoopNode.getHostname() );
                    }
                    config.setHadoopClusterName( hadoopInfo.getClusterName() );
                    config.setWorkers( new HashSet<UUID>() );
                    config.setCoordinatorNode( null );
                }
            }
        } );

        //coordinator selection change listener
        coordinatorComboChangeListener = new Property.ValueChangeListener()
        {

            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    ContainerHost coordinator = ( ContainerHost ) event.getProperty().getValue();
                    config.setCoordinatorNode( coordinator.getId() );

                    //clear workers
                    HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) hadoopClustersCombo.getValue();
                    if ( !CollectionUtil.isCollectionEmpty( config.getWorkers() ) )
                    {
                        config.getWorkers().remove( coordinator.getId() );
                    }
                    hadoopEnvironment = environmentManager.getEnvironmentByUUID( hadoopInfo.getEnvironmentId() );
                    Set<ContainerHost> hadoopNodes =
                            hadoopEnvironment.getContainerHostsByIds( Sets.newHashSet( hadoopInfo.getAllNodes() ) );
                    hadoopNodes.remove( coordinator );
                    workersSelect.getContainerDataSource().removeAllItems();
                    for ( ContainerHost hadoopNode : hadoopNodes )
                    {
                        workersSelect.getContainerDataSource().addItem( hadoopNode );
                    }
                    workersSelect.removeValueChangeListener( workersSelectChangeListener );
                    workersSelect.setValue( hadoopEnvironment.getContainerHostsByIds( config.getWorkers() ) );
                    workersSelect.addValueChangeListener( workersSelectChangeListener );
                }
            }
        };
        coordinatorNodeCombo.addValueChangeListener( coordinatorComboChangeListener );

        //workers selection change listener
        workersSelectChangeListener = new Property.ValueChangeListener()
        {

            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    Set<ContainerHost> nodes = ( Set<ContainerHost> ) event.getProperty().getValue();
                    Set<UUID> workerList = new HashSet<>();
                    for ( ContainerHost host : nodes )
                    {
                        workerList.add( host.getId() );
                    }
                    config.setWorkers( workerList );

                    //clear workers
                    if ( config.getCoordinatorNode() != null && config.getWorkers()
                                                                      .contains( config.getCoordinatorNode() ) )
                    {

                        config.setCoordinatorNode( null );
                        coordinatorNodeCombo.removeValueChangeListener( coordinatorComboChangeListener );
                        coordinatorNodeCombo.setValue( null );
                        coordinatorNodeCombo.addValueChangeListener( coordinatorComboChangeListener );
                    }
                    /*HadoopClusterConfig hadoopInfo = ( HadoopClusterConfig ) hadoopClustersCombo.getValue();
                    List<Agent> hadoopNodes = hadoopInfo.getAllNodes();
                    hadoopNodes.removeAll( config.getWorkers() );
                    coordinatorNodeCombo.removeAllItems();
                    for ( Agent agent : hadoopNodes )
                    {
                        coordinatorNodeCombo.addItem( agent );
                        coordinatorNodeCombo.setItemCaption( agent, agent.getHostname() );
                    }
                    if ( config.getCoordinatorNode() != null )
                    {
                        coordinatorNodeCombo.removeValueChangeListener( coordinatorComboChangeListener );
                        coordinatorNodeCombo.setValue( config.getCoordinatorNode() );
                        coordinatorNodeCombo.addValueChangeListener( coordinatorComboChangeListener );
                    }*/
                }
            }
        };
        workersSelect.addValueChangeListener( workersSelectChangeListener );

        parent.addComponent( hadoopClustersCombo );
        parent.addComponent( coordinatorNodeCombo );
        parent.addComponent( workersSelect );
    }


    private void addWithHadoopComponents( ComponentContainer parent, final PrestoClusterConfig config,
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
        PrestoClusterConfig config = wizard.getConfig();
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
            else if ( config.getCoordinatorNode() == null )
            {
                show( "Please, select coordinator node" );
            }
            else if ( CollectionUtil.isCollectionEmpty( config.getWorkers() ) )
            {
                show( "Please, select worker nodes" );
            }
            else
            {
                wizard.setHadoopConfig( hadoop.getCluster( wizard.getConfig().getHadoopClusterName() ) );
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
                wizard.setHadoopConfig( hadoop.getCluster( wizard.getConfig().getHadoopClusterName() ) );
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
