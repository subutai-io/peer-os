package org.safehaus.subutai.plugin.presto.ui.wizard;


import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.presto.api.Presto;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.api.SetupType;
import org.safehaus.subutai.server.ui.component.ProgressWindow;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;


public class VerificationStep extends Panel
{

    public VerificationStep( final Presto presto, final ExecutorService executorService, final Tracker tracker,
                             EnvironmentManager environmentManager, final Wizard wizard )
    {

        setSizeFull();

        GridLayout grid = new GridLayout( 1, 5 );
        grid.setSpacing( true );
        grid.setMargin( true );
        grid.setSizeFull();

        Label confirmationLbl = new Label( "<strong>Please verify the installation settings "
                + "(you may change them by clicking on Back button)</strong><br/>" );
        confirmationLbl.setContentMode( ContentMode.HTML );

        final PrestoClusterConfig config = wizard.getConfig();
        final HadoopClusterConfig hc = wizard.getHadoopConfig();

        ConfigView cfgView = new ConfigView( "Installation configuration" );
        cfgView.addStringCfg( "Installation name", config.getClusterName() );
        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            Environment hadoopEnvironment = environmentManager.getEnvironmentByUUID( hc.getEnvironmentId() );
            ContainerHost coordinator =
                    hadoopEnvironment.getContainerHostByUUID( wizard.getConfig().getCoordinatorNode() );
            Set<ContainerHost> workers = hadoopEnvironment.getHostsByIds( wizard.getConfig().getWorkers() );
            cfgView.addStringCfg( "Hadoop cluster Name", wizard.getConfig().getHadoopClusterName() );
            cfgView.addStringCfg( "Master Node", coordinator.getHostname() );
            for ( ContainerHost worker : workers )
            {
                cfgView.addStringCfg( "Slave nodes", worker.getHostname() + "" );
            }
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP )
        {
            cfgView.addStringCfg( "Hadoop cluster name", hc.getClusterName() );
            cfgView.addStringCfg( "Number of Hadoop slave nodes", hc.getCountOfSlaveNodes() + "" );
            cfgView.addStringCfg( "Replication factor", hc.getReplicationFactor() + "" );
            cfgView.addStringCfg( "Domain name", hc.getDomainName() );
        }

        Button install = new Button( "Install" );
        install.setId( "PresVerInstall" );
        install.addStyleName( "default" );
        install.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                UUID trackId = null;
                if ( config.getSetupType() == SetupType.OVER_HADOOP )
                {
                    trackId = presto.installCluster( config, hc );
                }
                else if ( config.getSetupType() == SetupType.WITH_HADOOP )
                {
                    trackId = presto.installCluster( config, hc );
                }

                ProgressWindow window =
                        new ProgressWindow( executorService, tracker, trackId, PrestoClusterConfig.PRODUCT_KEY );

                window.getWindow().addCloseListener( new Window.CloseListener()
                {
                    @Override
                    public void windowClose( Window.CloseEvent closeEvent )
                    {
                        wizard.init();
                    }
                } );
                getUI().addWindow( window.getWindow() );
            }
        } );

        Button back = new Button( "Back" );
        back.setId( "PresVerBack" );
        back.addStyleName( "default" );
        back.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                wizard.back();
            }
        } );

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent( back );
        buttons.addComponent( install );

        grid.addComponent( confirmationLbl, 0, 0 );

        grid.addComponent( cfgView.getCfgTable(), 0, 1, 0, 3 );

        grid.addComponent( buttons, 0, 4 );

        setContent( grid );
    }
}
