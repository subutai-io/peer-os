package org.safehaus.subutai.plugin.sqoop.ui.wizard;


import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.sqoop.api.SetupType;
import org.safehaus.subutai.plugin.sqoop.api.Sqoop;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;
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

    public VerificationStep( final Sqoop sqoop, final ExecutorService executorService, final Tracker tracker,
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

        final SqoopConfig config = wizard.getConfig();
        final HadoopClusterConfig hc = wizard.getHadoopConfig();
        ConfigView cfgView = new ConfigView( "Installation configuration" );
        cfgView.addStringCfg( "Installation name", wizard.getConfig().getClusterName() );
        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            Environment hadoopEnv = environmentManager.getEnvironmentByUUID( hc.getEnvironmentId() );
            Set<ContainerHost> hosts = hadoopEnv.getContainerHostsByIds( config.getNodes() );
            for ( ContainerHost host : hosts )
            {
                cfgView.addStringCfg( "Node(s) to install", host.getHostname() );
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
        install.setId( "sqoopVerificationInstall" );
        install.addStyleName( "default" );
        install.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {

                UUID trackId = null;
                if ( config.getSetupType() == SetupType.OVER_HADOOP )
                {
                    trackId = sqoop.installCluster( wizard.getConfig() );
                }
                else if ( config.getSetupType() == SetupType.WITH_HADOOP )
                {
                    trackId = sqoop.installCluster( config, hc );
                }

                ProgressWindow window
                        = new ProgressWindow( executorService, tracker, trackId, SqoopConfig.PRODUCT_KEY );
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
        back.setId( "sqoopVerificationBack" );
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
        buttons.addComponent( install );

        grid.addComponent( confirmationLbl, 0, 0 );
        grid.addComponent( cfgView.getCfgTable(), 0, 1, 0, 3 );
        grid.addComponent( buttons, 0, 4 );

        setContent( grid );
    }
}

