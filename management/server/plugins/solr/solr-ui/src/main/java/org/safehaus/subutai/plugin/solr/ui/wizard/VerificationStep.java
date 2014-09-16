/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.solr.ui.wizard;


import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.solr.api.Solr;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
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

    public VerificationStep( final Solr solr, final ExecutorService executorService, final Tracker tracker,
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
        cfgView.addStringCfg( "Installation Name", wizard.getSolrClusterConfig().getClusterName() );

        Button install = new Button( "Install" );
        install.addStyleName( "default" );
        install.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                UUID trackID = solr.installCluster( wizard.getSolrClusterConfig() );
                ProgressWindow window =
                        new ProgressWindow( executorService, tracker, trackID, SolrClusterConfig.PRODUCT_KEY );
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
