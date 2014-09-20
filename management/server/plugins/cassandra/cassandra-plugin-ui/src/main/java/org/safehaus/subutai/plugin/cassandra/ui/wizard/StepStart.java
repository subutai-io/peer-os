/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.cassandra.ui.wizard;


import org.safehaus.subutai.common.util.FileUtil;

import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;


/**
 * @author dilshat
 */
public class StepStart extends VerticalLayout
{

    public StepStart( final Wizard wizard )
    {
        setSizeFull();

        GridLayout gridLayout = new GridLayout( 10, 6 );
        gridLayout.setSizeFull();

        Label welcomeMsg = new Label( "<center><h2>Welcome to Cassandra Installation Wizard.</h2></center>" );
        welcomeMsg.addStyleName( "h2" );
        welcomeMsg.setContentMode( ContentMode.HTML );
        gridLayout.addComponent( welcomeMsg, 3, 1, 6, 2 );

        Label logoImg = new Label();
        logoImg.setIcon( new FileResource( FileUtil.getFile( wizard.getCassandraUI().MODULE_IMAGE, this ) ) );
        logoImg.setContentMode( ContentMode.HTML );
        logoImg.setHeight( 150, Unit.PIXELS );
        logoImg.setWidth( 220, Unit.PIXELS );
        gridLayout.addComponent( logoImg, 1, 3, 2, 5 );

        Button next = new Button( "Start" );
        next.addStyleName( "default" );
        next.setWidth( 100, Unit.PIXELS );
        gridLayout.addComponent( next, 6, 4, 6, 4 );
        gridLayout.setComponentAlignment( next, Alignment.BOTTOM_RIGHT );

        next.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                wizard.getConfig().reset();
                wizard.next();
            }
        } );

        addComponent( gridLayout );
    }
}
