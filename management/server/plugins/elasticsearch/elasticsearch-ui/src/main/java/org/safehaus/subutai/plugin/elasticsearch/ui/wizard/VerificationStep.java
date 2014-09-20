package org.safehaus.subutai.plugin.elasticsearch.ui.wizard;


import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.elasticsearch.api.Elasticsearch;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
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

    public VerificationStep( final Elasticsearch elasticsearch, final ExecutorService executorService,
                             final Tracker tracker, final Wizard wizard )
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
        cfgView.addStringCfg( "Cluster Name: ", wizard.getConfig().getClusterName() );
        cfgView.addStringCfg( "Number of Nodes: ", "" + wizard.getConfig().getNumberOfNodes() );
        cfgView.addStringCfg( "Number of Master Nodes: ", "" + wizard.getConfig().getNumberOfMasterNodes() );
        cfgView.addStringCfg( "Number of Data Nodes: ", "" + wizard.getConfig().getNumberOfDataNodes() );
        cfgView.addStringCfg( "Number of Shards: ", "" + wizard.getConfig().getNumberOfShards() );
        cfgView.addStringCfg( "Number of Replicas: ", "" + wizard.getConfig().getNumberOfReplicas() );

        Button installButton = new Button( "Install" );
        installButton.addStyleName( "default" );
        installButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                UUID trackID = elasticsearch.installCluster( wizard.getConfig() );
                ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                        ElasticsearchClusterConfiguration.PRODUCT_KEY );
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
        buttons.addComponent( installButton );

        grid.addComponent( confirmationLbl, 0, 0 );

        grid.addComponent( cfgView.getCfgTable(), 0, 1, 0, 3 );

        grid.addComponent( buttons, 0, 4 );

        addComponent( grid );
    }
}
