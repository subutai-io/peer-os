/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hbase.ui.wizard;


import java.util.UUID;

import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;
import org.safehaus.subutai.server.ui.component.ProgressWindow;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


/**
 * @author dilshat
 */
public class VerificationStep extends VerticalLayout {

    public VerificationStep( final Wizard wizard ) {

        setSizeFull();

        GridLayout grid = new GridLayout( 1, 5 );
        grid.setSpacing( true );
        grid.setMargin( true );
        grid.setSizeFull();

        Label confirmationLbl = new Label( "<strong>Please verify the installation settings "
                + "(You may change them by clicking on Back button)</strong><br/>" );
        confirmationLbl.setContentMode( ContentMode.HTML );

        ConfigView cfgView = new ConfigView( "Installation configuration" );
        cfgView.addStringCfg( "Cluster Name", wizard.getConfig().getClusterName() );
        cfgView.addStringCfg( "Master", wizard.getConfig().getMaster() + "\n" );
        for ( String hostname : wizard.getConfig().getRegion() ) {
            cfgView.addStringCfg( "Region", hostname + "\n" );
        }
        for ( String hostname : wizard.getConfig().getQuorum() ) {
            cfgView.addStringCfg( "Quorum", hostname + "\n" );
        }
        cfgView.addStringCfg( "Backup master", wizard.getConfig().getBackupMasters() + "\n" );
        cfgView.addStringCfg( "Hadoop name node", wizard.getConfig().getHadoopNameNode() + "\n" );

        Button install = new Button( "Install" );
        install.addStyleName( "default" );
        install.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                UUID trackID = wizard.gethBaseUI().getHbaseManager().installCluster( wizard.getConfig() );
                ProgressWindow window = new ProgressWindow( wizard.gethBaseUI().getExecutor(), wizard.gethBaseUI().getTracker(), trackID,
                        HBaseClusterConfig.PRODUCT_KEY );
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
            public void buttonClick( Button.ClickEvent clickEvent ) {
                wizard.back();
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
