/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.accumulo.ui.wizard;


import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.accumulo.api.Accumulo;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.api.SetupType;
import org.safehaus.subutai.server.ui.component.ProgressWindow;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;


/**
 * @author dilshat
 */
public class VerificationStep extends Panel {

    public VerificationStep( final Accumulo accumulo, final ExecutorService executorService, final Tracker tracker,
                             final Wizard wizard ) {

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
        cfgView.addStringCfg( "Instance name", wizard.getConfig().getInstanceName() );
        cfgView.addStringCfg( "Password", wizard.getConfig().getPassword() );
        cfgView.addStringCfg( "Hadoop cluster", wizard.getConfig().getHadoopClusterName() );
        cfgView.addStringCfg( "Zookeeper cluster", wizard.getConfig().getZookeeperClusterName() );

        if ( wizard.getConfig().getSetupType() == SetupType.OVER_HADOOP_N_ZK ) {
            cfgView.addStringCfg( "Master node", wizard.getConfig().getMasterNode().getHostname() );
            cfgView.addStringCfg( "GC node", wizard.getConfig().getGcNode().getHostname() );
            cfgView.addStringCfg( "Monitor node", wizard.getConfig().getMonitor().getHostname() );
            for ( Agent agent : wizard.getConfig().getTracers() ) {
                cfgView.addStringCfg( "Tracers", agent.getHostname() );
            }
            for ( Agent agent : wizard.getConfig().getSlaves() ) {
                cfgView.addStringCfg( "Slaves", agent.getHostname() );
            }
        }
        else {
            cfgView.addStringCfg( "Number of Hadoop slaves",
                    wizard.getHadoopClusterConfig().getCountOfSlaveNodes() + "" );
            cfgView.addStringCfg( "Hadoop replication factor",
                    wizard.getHadoopClusterConfig().getReplicationFactor() + "" );
            cfgView.addStringCfg( "Hadoop domain name", wizard.getHadoopClusterConfig().getDomainName() + "" );
            cfgView.addStringCfg( "Number of tracers", wizard.getConfig().getNumberOfTracers() + "" );
            cfgView.addStringCfg( "Number of slaves", wizard.getConfig().getNumberOfSlaves() + "" );
        }

        Button install = new Button( "Install" );
        install.addStyleName( "default" );
        install.addClickListener( new Button.ClickListener() {

            @Override
            public void buttonClick( Button.ClickEvent event ) {

                UUID trackID = wizard.getConfig().getSetupType() == SetupType.OVER_HADOOP_N_ZK ?
                               accumulo.installCluster( wizard.getConfig() ) :
                               accumulo.installCluster( wizard.getConfig(), wizard.getHadoopClusterConfig(),
                                       wizard.getZookeeperClusterConfig() );
                ProgressWindow window =
                        new ProgressWindow( executorService, tracker, trackID, AccumuloClusterConfig.PRODUCT_KEY );
                window.getWindow().addCloseListener( new Window.CloseListener() {
                    @Override
                    public void windowClose( Window.CloseEvent closeEvent ) {
                        wizard.init();
                    }
                } );
                getUI().addWindow( window.getWindow() );
            }
        } );

        Button back = new Button( "Back" );
        back.addStyleName( "default" );
        back.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent event ) {
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
