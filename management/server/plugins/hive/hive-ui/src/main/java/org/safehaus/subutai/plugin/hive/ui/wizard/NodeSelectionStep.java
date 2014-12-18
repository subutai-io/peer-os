package org.safehaus.subutai.plugin.hive.ui.wizard;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hive.api.Hive;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.api.SetupType;
import org.safehaus.subutai.server.ui.api.PortalModuleService;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


public class NodeSelectionStep extends Panel
{

    private final Hive hive;
    private final Hadoop hadoop;
    private int controlWidth = 350;
    private EnvironmentManager environmentManager;


    public NodeSelectionStep( final Hive hive, final Hadoop hadoop, final EnvironmentManager environmentManager,
                              final Wizard wizard )
    {

        this.hive = hive;
        this.hadoop = hadoop;
        this.environmentManager = environmentManager;

        setSizeFull();

        GridLayout content = new GridLayout( 1, 2 );
        content.setSizeFull();
        content.setSpacing( true );
        content.setMargin( true );

        TextField nameTxt = new TextField( "Cluster name" );
        nameTxt.setId( "HiveClusterName" );
        nameTxt.setRequired( true );
        nameTxt.setWidth( controlWidth, Unit.POINTS );
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
        next.setId( "HiveNodeSelectionNext" );
        next.addStyleName( "default" );
        next.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                nextButtonClickHandler( wizard );
            }
        } );

        Button back = new Button( "Back" );
        back.setId( "HiveNodeSelectionBack" );
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
            if ( !addOverHadoopComponents( content, wizard.getConfig() ) )
            {
                wizard.back();
            }
        }
        else if ( wizard.getConfig().getSetupType() == SetupType.WITH_HADOOP )
        {
            addWithHadoopComponents( content, wizard.getConfig(), wizard.getHadoopConfig() );
        }
        content.addComponent( buttons );

        setContent( layout );
    }


    private boolean addOverHadoopComponents( ComponentContainer parent, final HiveConfig config )
    {
        ComboBox hadoopClusters = new ComboBox( "Hadoop cluster" );
        hadoopClusters.setId( "HiveHadoopClusterCb" );
        final ComboBox cmbServerNode = makeServerNodeComboBox( config );

        hadoopClusters.setImmediate( true );
        hadoopClusters.setWidth( controlWidth, Unit.POINTS );
        hadoopClusters.setTextInputAllowed( false );
        hadoopClusters.setRequired( true );
        hadoopClusters.setNullSelectionAllowed( false );
        hadoopClusters.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() != null )
                {
                    HadoopClusterConfig hc = ( HadoopClusterConfig ) event.getProperty().getValue();
                    config.setHadoopClusterName( hc.getClusterName() );
                    config.setHadoopNodes( new HashSet<>( hc.getAllNodes() ) );

                    ContainerHost selected = null;
                    if ( config.getServer() != null )
                    {
                        selected = environmentManager.getEnvironmentByUUID( config.getEnvironmentId() )
                                                     .getContainerHostById( config.getServer() );
                    }
                    else
                    {
                        selected = environmentManager.getEnvironmentByUUID( hc.getEnvironmentId() )
                                                     .getContainerHostById( hc.getNameNode() );
                    }
                    fillServerNodeComboBox( config, cmbServerNode, hc, selected );
                    filterNodes( cmbServerNode, hc );
                    // TODO if all nodes are filtered, than notify user
                }
            }
        } );

        List<HadoopClusterConfig> clusters = hadoop.getClusters();
        if ( !clusters.isEmpty() )
        {
            for ( HadoopClusterConfig hci : clusters )
            {
                hadoopClusters.addItem( hci );
                hadoopClusters.setItemCaption( hci, hci.getClusterName() );
            }
        }
        else
        {
            PortalModuleService portalModuleService = null;
            // get bundle instance via the OSGi Framework Util class
            BundleContext ctx = FrameworkUtil.getBundle( PortalModuleService.class ).getBundleContext();
            if ( ctx != null )
            {
                ServiceReference serviceReference = ctx.getServiceReference( PortalModuleService.class.getName() );
                if ( serviceReference != null )
                {
                    portalModuleService = PortalModuleService.class.cast( ctx.getService( serviceReference ) );
                    //                    return PortalModuleService.class.cast( ctx.getService( serviceReference ) );
                }
            }
            if ( portalModuleService != null )
            {
                portalModuleService.loadDependentModule( HadoopClusterConfig.PRODUCT_KEY );
            }
            return false;
            //            MainUI mainUI = MainUI.getInstance();
            //            ModulesView modulesView = mainUI.getModulesView();
            //            HashMap<String, AbstractLayout> moduleViews = modulesView.getModuleViews();
            //            ModuleView moduleView = ( ModuleView ) moduleViews.get( HadoopClusterConfig.PRODUCT_KEY );
            //            moduleView.addModuleTab();
        }

        String hn = config.getHadoopClusterName();
        if ( hn != null && !hn.isEmpty() )
        {
            HadoopClusterConfig info = hadoop.getCluster( hn );
            if ( info != null )
            {
                hadoopClusters.setValue( info );
            }
        }

        parent.addComponent( hadoopClusters );
        parent.addComponent( cmbServerNode );
        return true;
    }


    private ComboBox makeServerNodeComboBox( final HiveConfig config )
    {
        ComboBox cb = new ComboBox( "Server node" );
        cb.setId( "HiveserverNodecb" );
        cb.setImmediate( true );
        cb.setTextInputAllowed( false );
        cb.setRequired( true );
        cb.setNullSelectionAllowed( false );
        cb.setWidth( controlWidth, Unit.POINTS );
        cb.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                UUID hiveMaster = ( UUID ) event.getProperty().getValue();
                config.setServer( hiveMaster );
                config.getClients().clear();
                config.getClients().addAll( config.getHadoopNodes() );
                config.getClients().remove( hiveMaster );
            }
        } );
        return cb;
    }


    private void fillServerNodeComboBox( HiveConfig config, ComboBox serverNode, HadoopClusterConfig hadoopInfo,
                                         ContainerHost selected )
    {
        serverNode.removeAllItems();
        List<UUID> slaves = hadoopInfo.getAllSlaveNodes();
        for ( UUID uuid : hadoopInfo.getAllNodes() )
        {
            serverNode.addItem( getHost( hadoopInfo, uuid ).getId() );
            // TODO
            String caption = getHost( hadoopInfo, uuid ).getHostname();
            if ( hadoopInfo.getJobTracker().equals( uuid ) )
            {
                caption += " [JobTracker]";
            }
            else if ( hadoopInfo.getNameNode().equals( uuid ) )
            {
                caption += " [NameNode]";
            }
            else if ( hadoopInfo.getSecondaryNameNode().equals( uuid ) )
            {
                caption += " [SecondaryNameNode]";
            }
            else if ( slaves.contains( uuid ) )
            {
                caption += " [Slave Node]";
            }
            serverNode.setItemCaption( getHost( hadoopInfo, uuid ).getId(), caption );
        }
        if ( selected != null )
        {
            serverNode.setValue( selected.getId() );
        }
    }


    private ContainerHost getHost( HadoopClusterConfig config, UUID uuid )
    {
        return environmentManager.getEnvironmentByUUID( config.getEnvironmentId() ).getContainerHostById( uuid );
    }


    private void filterNodes( final ComboBox serverNode, final HadoopClusterConfig hadoopClusterConfig )
    {
        Collection<UUID> items = ( Collection<UUID> ) serverNode.getItemIds();
        final Set<UUID> set = new HashSet<>( items );
        for ( final UUID uuid : set )
        {
            new Thread( new Runnable()
            {
                @Override
                public void run()
                {
                    ContainerHost host =
                            environmentManager.getEnvironmentByUUID( hadoopClusterConfig.getEnvironmentId() )
                                              .getContainerHostById( uuid );
                    boolean isInstalled = hive.isInstalled( hadoopClusterConfig.getClusterName(), host.getHostname() );
                    if ( isInstalled )
                    {
                        serverNode.removeItem( uuid );
                    }
                }
            } ).start();
        }
    }


    private void addWithHadoopComponents( ComponentContainer content, final HiveConfig config,
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

        content.addComponent( new Label( "Hadoop settings" ) );
        content.addComponent( txtHadoopClusterName );
        content.addComponent( cmbSlaveNodes );
        content.addComponent( cmbReplFactor );
        content.addComponent( txtHadoopDomain );
    }


    private void nextButtonClickHandler( Wizard wizard )
    {
        HiveConfig config = wizard.getConfig();
        if ( config.getClusterName() == null || config.getClusterName().isEmpty() )
        {
            show( "Enter name for Hive installation" );
            return;
        }
        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            String name = config.getHadoopClusterName();
            if ( name == null || name.isEmpty() )
            {
                show( "Select Hadoop cluster" );
            }
            else if ( config.getServer() == null )
            {
                show( "Select server node" );
            }
            else if ( config.getClients() == null || config.getClients().isEmpty() )
            {
                show( "Select client nodes" );
            }
            else
            {
                wizard.setHadoopConfig( hadoop.getCluster( config.getHadoopClusterName() ) );
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
