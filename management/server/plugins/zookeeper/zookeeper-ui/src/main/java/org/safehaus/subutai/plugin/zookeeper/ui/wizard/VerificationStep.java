/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.zookeeper.ui.wizard;


import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.Zookeeper;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
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

    public VerificationStep( final Zookeeper zookeeper, final ExecutorService executorService, final Tracker tracker,
                             final Wizard wizard, EnvironmentManager environmentManager )
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
        if ( wizard.getConfig().getSetupType() == SetupType.STANDALONE )
        {
            cfgView.addStringCfg( "Number of nodes", wizard.getConfig().getNumberOfNodes() + "" );
        }
        else if ( wizard.getConfig().getSetupType() == SetupType.OVER_HADOOP )
        {
            Environment hadoopEnvironment =
                    environmentManager.getEnvironmentByUUID( wizard.getHadoopClusterConfig().getEnvironmentId() );
            Set<ContainerHost> zookeeperNodes = hadoopEnvironment.getHostsByIds( wizard.getConfig().getNodes() );
            cfgView.addStringCfg( "Hadoop cluster name", wizard.getConfig().getHadoopClusterName() );
            for ( ContainerHost node : zookeeperNodes )
            {

                cfgView.addStringCfg( "Nodes to install", node.getHostname() );
            }
        }
        else if ( wizard.getConfig().getSetupType() == SetupType.WITH_HADOOP )
        {
            cfgView.addStringCfg( "Zookeeper cluster name", wizard.getConfig().getClusterName() );
            cfgView.addStringCfg( "Number of Zookeeper cluster nodes", wizard.getConfig().getNumberOfNodes() + "" );
            cfgView.addStringCfg( "Hadoop cluster name", wizard.getHadoopClusterConfig().getClusterName() );
            cfgView.addStringCfg( "Number of Hadoop slave nodes",
                    wizard.getHadoopClusterConfig().getCountOfSlaveNodes() + "" );
            cfgView.addStringCfg( "Replication factor for Hadoop slave nodes",
                    wizard.getHadoopClusterConfig().getReplicationFactor() + "" );
            cfgView.addStringCfg( "Hadoop cluster domain name", wizard.getHadoopClusterConfig().getDomainName() );
        }

        Button install = new Button( "Install" );
        install.setId( "ZookeeperInstInstall" );
        install.addStyleName( "default" );
        install.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                UUID trackID = wizard.getConfig().getSetupType() == SetupType.WITH_HADOOP ?
                               zookeeper.installCluster( wizard.getConfig() ) :
                               zookeeper.installCluster( wizard.getConfig() );

                ProgressWindow window =
                        new ProgressWindow( executorService, tracker, trackID, ZookeeperClusterConfig.PRODUCT_KEY );
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
        back.setId( "ZookeeperInstBack" );
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
