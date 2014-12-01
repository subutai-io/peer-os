package org.safehaus.subutai.plugin.spark.ui.wizard;


import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.plugin.spark.ui.SparkPortalModule;

import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;


public class WelcomeStep extends Panel
{

    public WelcomeStep( final Wizard wizard )
    {

        setSizeFull();

        GridLayout grid = new GridLayout( 10, 6 );
        grid.setSpacing( true );
        grid.setMargin( true );
        grid.setSizeFull();

        Label welcomeMsg = new Label( "<center><h2>Welcome to Spark Installation Wizard!</h2>" );
        welcomeMsg.setContentMode( ContentMode.HTML );
        grid.addComponent( welcomeMsg, 3, 1, 6, 2 );

        Label logoImg = new Label();
        logoImg.setIcon( new FileResource( FileUtil.getFile( SparkPortalModule.MODULE_IMAGE, this ) ) );
        logoImg.setContentMode( ContentMode.HTML );
        logoImg.setHeight( 100, Unit.PIXELS );
        logoImg.setWidth( 192, Unit.PIXELS );
        grid.addComponent( logoImg, 1, 3, 2, 5 );

        Button next = new Button( "Start Spark installation" );
        next.setId( "sparkStartOverHadoop" );
        next.addStyleName( "default" );
        next.addClickListener( new NextClickHandler( wizard ) );
        grid.addComponent( next, 4, 4, 4, 4 );
        grid.setComponentAlignment( next, Alignment.BOTTOM_RIGHT );

        setContent( grid );
    }


    private class NextClickHandler implements Button.ClickListener
    {

        private final Wizard wizard;


        public NextClickHandler( Wizard wizard )
        {
            this.wizard = wizard;
        }


        @Override
        public void buttonClick( Button.ClickEvent clickEvent )
        {
            wizard.init();
            wizard.next();
        }
    }
}
