/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.mahout.ui.wizard;


import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.plugin.mahout.api.SetupType;
import org.safehaus.subutai.plugin.mahout.ui.MahoutUI;

import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;


/**
 * @author dilshat
 */
public class WelcomeStep extends Panel {

    public WelcomeStep( final Wizard wizard ) {

        setSizeFull();

        GridLayout grid = new GridLayout( 10, 6 );
        grid.setSpacing( true );
        grid.setMargin( true );
        grid.setSizeFull();

        Label welcomeMsg = new Label( "<center><h2>Welcome to Oozie Installation Wizard!</h2>" );
        welcomeMsg.setContentMode( ContentMode.HTML );
        grid.addComponent( welcomeMsg, 3, 1, 6, 2 );

        Label logoImg = new Label();
        // Image as a file resource
        logoImg.setIcon( new FileResource( FileUtil.getFile( MahoutUI.MODULE_IMAGE, this ) ) );
        logoImg.setContentMode( ContentMode.HTML );
        logoImg.setHeight( 56, Unit.PIXELS );
        logoImg.setWidth( 220, Unit.PIXELS );
        grid.addComponent( logoImg, 1, 3, 2, 5 );

        Button startOverHadoopNZK = new Button( "Start over Hadoop installation" );
        startOverHadoopNZK.addStyleName( "default" );
        grid.addComponent( startOverHadoopNZK, 7, 4, 7, 4 );
        grid.setComponentAlignment( startOverHadoopNZK, Alignment.BOTTOM_RIGHT );
        Button startWithHadoopNZK = new Button( "Start with Hadoop installation" );
        startWithHadoopNZK.addStyleName( "default" );
        grid.addComponent( startWithHadoopNZK, 8, 4, 8, 4 );
        grid.setComponentAlignment( startWithHadoopNZK, Alignment.BOTTOM_RIGHT );

        startOverHadoopNZK.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent event ) {
                wizard.init();
                wizard.getConfig().setSetupType( SetupType.OVER_HADOOP );
                wizard.next();
            }
        } );
        startWithHadoopNZK.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent event ) {
                wizard.init();
                wizard.getConfig().setSetupType( SetupType.WITH_HADOOP );
                wizard.next();
            }
        } );

        setContent( grid );
    }
}
