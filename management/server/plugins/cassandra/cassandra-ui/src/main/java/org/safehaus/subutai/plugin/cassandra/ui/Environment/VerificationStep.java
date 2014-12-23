package org.safehaus.subutai.plugin.cassandra.ui.Environment;


import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.cassandra.api.Cassandra;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.server.ui.component.ProgressWindow;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


public class VerificationStep extends VerticalLayout
{

    public VerificationStep( final Cassandra cassandra, final ExecutorService executorService, final Tracker tracker,
                             final EnvironmentWizard environmentWizard )
    {

        setSizeFull();

        GridLayout grid = new GridLayout( 1, 5 );
        grid.setSpacing( true );
        grid.setMargin( true );
        grid.setSizeFull();

        Label confirmationLbl = new Label( "<strong>Please verify the installation settings "
                + "(You may change them by clicking on Back button)</strong><br/>" );
        confirmationLbl.setContentMode( ContentMode.HTML );

        ConfigView cfgView = new ConfigView( "Installation configuration" );
        cfgView.addStringCfg( "Cluster Name", environmentWizard.getConfig().getClusterName() );
        cfgView.addStringCfg( "Domain Name", environmentWizard.getConfig().getDomainName() );
        cfgView.addStringCfg( "Data directory", environmentWizard.getConfig().getDataDirectory() );
        cfgView.addStringCfg( "Saved caches directory", environmentWizard.getConfig().getSavedCachesDirectory() );
        cfgView.addStringCfg( "Commit log directory", environmentWizard.getConfig().getCommitLogDirectory() );

        String selectedNodes = "";
        final Environment environment = environmentWizard.getEnvironmentManager().getEnvironmentByUUID(
                environmentWizard.getConfig().getEnvironmentId() );
        for ( UUID uuid : environmentWizard.getConfig().getNodes() ){
            selectedNodes += environment.getContainerHostById( uuid ).getHostname() + ",";
        }

        String seeds = "";
        for ( UUID uuid : environmentWizard.getConfig().getSeedNodes() ){
            seeds += environment.getContainerHostById( uuid ).getHostname() + ",";
        }

        cfgView.addStringCfg( "Nodes to be configured", selectedNodes.substring( 0, ( selectedNodes.length() - 1 ) ) );
        cfgView.addStringCfg( "Seed nodes", seeds.substring( 0, ( seeds.length() -1 ) ) + "" );
        cfgView.addStringCfg( "Environment UUID", environmentWizard.getConfig().getEnvironmentId() + "" );

        Button install = new Button( "Configure" );
        install.addStyleName( "default" );
        install.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                UUID trackID = cassandra.configureEnvironmentCluster( environmentWizard.getConfig() );
                ProgressWindow window =
                        new ProgressWindow( executorService, tracker, trackID, CassandraClusterConfig.PRODUCT_KEY );
                window.getWindow().addCloseListener( new Window.CloseListener()
                {
                    @Override
                    public void windowClose( Window.CloseEvent closeEvent )
                    {
                        environmentWizard.init();
                    }
                } );
                getUI().addWindow( window.getWindow() );
            }
        } );

        Button back = new Button( "Back" );
        back.addStyleName( "default" );
        back.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                environmentWizard.clearConfig();
                environmentWizard.back();
            }
        } );

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent( back );
        buttons.addComponent( install );

        grid.addComponent( confirmationLbl, 0, 0 );

        grid.addComponent( cfgView.getCfgTable(), 0, 1, 0, 3 );

        grid.addComponent( buttons, 0, 4 );

        addComponent( grid );
    }
}
