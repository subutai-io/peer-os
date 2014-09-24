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
public class StepStart extends Panel
{

    public StepStart( final Wizard wizard )
    {

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

        Button startOverHadoop = new Button( "Start over Hadoop installation" );
        startOverHadoop.addStyleName( "default" );
        grid.addComponent( startOverHadoop, 7, 4, 7, 4 );
        grid.setComponentAlignment( startOverHadoop, Alignment.BOTTOM_RIGHT );
        Button startWithHadoop = new Button( "Start with Hadoop installation" );
        startWithHadoop.addStyleName( "default" );
        grid.addComponent( startWithHadoop, 8, 4, 8, 4 );
        grid.setComponentAlignment( startWithHadoop, Alignment.BOTTOM_RIGHT );

        startOverHadoop.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                wizard.init();
                wizard.getConfig().setSetupType( SetupType.OVER_HADOOP );
                wizard.next();
            }
        } );
        startWithHadoop.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                wizard.init();
                wizard.getConfig().setSetupType( SetupType.WITH_HADOOP );
                wizard.next();
            }
        } );

        setContent( grid );
    }
}
