package org.safehaus.subutai.plugin.oozie.ui.wizard;


import java.util.UUID;

import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
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

    public VerificationStep( final Wizard wizard )
    {

        setSizeFull();

        GridLayout grid = new GridLayout( 1, 5 );
        grid.setSpacing( true );
        grid.setMargin( true );
        grid.setSizeFull();

        Label confirmationLbl = new Label( "<strong>Please verify the installation settings "
                + "(you may change them by clicking on Back button)</strong><br/>" );
        confirmationLbl.setContentMode( ContentMode.HTML );

        ConfigView cfgView = new ConfigView( "Installation configuration" );
        cfgView.addStringCfg( "Cluster Name", wizard.getConfig().getClusterName() );
        cfgView.addStringCfg( "Server", wizard.getConfig().getServer().toString() + "\n" );
        if ( wizard.getConfig().getClients() != null )
        {
            for ( UUID agent : wizard.getConfig().getClients() )
            {
                cfgView.addStringCfg( "Clients", agent.toString() + "\n" );
            }
        }

        Button install = new Button( "Install" );
        install.setId( "OozieVerificationInstall" );
        install.addStyleName( "default" );
        install.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                UUID trackID = wizard.getOozieManager().installCluster( wizard.getConfig() );
                final ProgressWindow window = new ProgressWindow( wizard.getExecutor(), wizard.getTracker(), trackID,
                        OozieClusterConfig.PRODUCT_KEY );
                final ConfirmationDialog alert = new ConfirmationDialog(
                        String.format( "Do you want to restart the %s hadoop cluster for changes to take effect?",
                                wizard.getConfig().getHadoopClusterName() ), "Yes", "No" );

                alert.getOk().addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent event )
                    {
                        Hadoop hadoopManager = wizard.getHadoopManager();
                        String hadoopClusterName = wizard.getConfig().getHadoopClusterName();
                        HadoopClusterConfig cluster = hadoopManager.getCluster( hadoopClusterName );
//                        UUID trackID = hadoopManager.restartNameNode( cluster );
                        /*ProgressWindow window = new ProgressWindow( wizard.getExecutor(), wizard.getTracker(), trackID,
                                HadoopClusterConfig.PRODUCT_KEY );
                        getUI().addWindow( window.getWindow() );*/
                    }
                } );


                alert.getAlert().addCloseListener( new Window.CloseListener()
                {
                    @Override
                    public void windowClose( final Window.CloseEvent e )
                    {
                        wizard.init();
                    }
                } );

                window.getWindow().addCloseListener( new Window.CloseListener()
                {
                    @Override
                    public void windowClose( Window.CloseEvent closeEvent )
                    {
                        getUI().addWindow( alert.getAlert() );
                    }
                } );
                getUI().addWindow( window.getWindow() );
            }
        } );

        Button back = new Button( "Back" );
        back.setId( "OozieVerificationBack" );
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
