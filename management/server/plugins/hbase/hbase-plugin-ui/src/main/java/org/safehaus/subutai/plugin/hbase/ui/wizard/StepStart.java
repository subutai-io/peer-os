/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hbase.ui.wizard;


import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.plugin.hbase.api.SetupType;
import org.safehaus.subutai.plugin.hbase.ui.HBaseUI;

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
public class StepStart extends Panel {

    public StepStart( final Wizard wizard ) {
        /*setSizeFull();
        GridLayout gridLayout = new GridLayout( 10, 6 );
        gridLayout.setSizeFull();

        Label welcomeMsg = new Label( "<center><h2>Welcome to HBase Installation Wizard!</h2><br/>"
                + "Please click Start button to continue</center>" );
        welcomeMsg.setContentMode( ContentMode.HTML );
        gridLayout.addComponent( welcomeMsg, 3, 1, 6, 2 );

        Label logoImg = new Label();
        logoImg.setIcon( new FileResource( FileUtil.getFile( HBaseUI.MODULE_IMAGE, this ) ) );
        logoImg.setContentMode( ContentMode.HTML );
        logoImg.setHeight( 150, Unit.PIXELS );
        logoImg.setWidth( 220, Unit.PIXELS );
        gridLayout.addComponent( logoImg, 1, 3, 2, 5 );

        HorizontalLayout hl = new HorizontalLayout();

        Button overHadoop = new Button( "Start over Hadoop Installation" );
        overHadoop.addStyleName( "default" );
        overHadoop.setWidth( 100, Unit.PIXELS );
        overHadoop.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                wizard.init();
                wizard.next();
            }
        } );

        hl.addComponent( overHadoop );

        Button withHadoop = new Button( "With Hadoop Installation" );
        withHadoop.addStyleName( "default" );
        withHadoop.setWidth( 100, Unit.PIXELS );
        withHadoop.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                wizard.init();
                wizard.next();
            }
        } );

        hl.addComponent( withHadoop );

        gridLayout.addComponent( hl, 6, 4, 6, 4 );
        addComponent( gridLayout );*/
        setSizeFull();

        GridLayout grid = new GridLayout( 10, 6 );
        grid.setSpacing( true );
        grid.setMargin( true );
        grid.setSizeFull();

        Label welcomeMsg = new Label( "<center><h2>Welcome to HBase Installation Wizard!</h2>" );
        welcomeMsg.setContentMode( ContentMode.HTML );
        grid.addComponent( welcomeMsg, 3, 1, 6, 2 );

        Label logoImg = new Label();
        // Image as a file resource
        logoImg.setIcon( new FileResource( FileUtil.getFile( HBaseUI.MODULE_IMAGE, this ) ) );
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
