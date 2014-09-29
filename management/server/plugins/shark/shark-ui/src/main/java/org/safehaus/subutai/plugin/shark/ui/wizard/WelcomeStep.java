package org.safehaus.subutai.plugin.shark.ui.wizard;


import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.plugin.shark.api.SetupType;
import org.safehaus.subutai.plugin.shark.ui.SharkPortalModule;


public class WelcomeStep extends Panel
{

    public WelcomeStep( final Wizard wizard )
    {

        setSizeFull();

        GridLayout grid = new GridLayout( 10, 6 );
        grid.setSpacing( true );
        grid.setMargin( true );
        grid.setSizeFull();

        Label welcomeMsg = new Label( "<center><h2>Welcome to Shark Installation Wizard!</h2>" );
        welcomeMsg.setContentMode( ContentMode.HTML );
        grid.addComponent( welcomeMsg, 3, 1, 6, 2 );

        Label logoImg = new Label();
        logoImg.setIcon( new FileResource( FileUtil.getFile( SharkPortalModule.MODULE_IMAGE, this ) ) );
        logoImg.setContentMode( ContentMode.HTML );
        logoImg.setHeight( 363, Unit.PIXELS );
        logoImg.setWidth( 88, Unit.PIXELS );
        grid.addComponent( logoImg, 1, 3, 2, 5 );

        Button next = new Button( "Start over-Spark installation" );
        next.addStyleName( "default" );
        next.addClickListener( new NextButtonClickHandler( wizard, SetupType.OVER_SPARK ) );
        grid.addComponent( next, 6, 4, 6, 4 );
        grid.setComponentAlignment( next, Alignment.BOTTOM_RIGHT );

        Button next2 = new Button( "Start with-Spark installation" );
        next2.addStyleName( "default" );
        next2.addClickListener( new NextButtonClickHandler( wizard, SetupType.WITH_HADOOP_SPARK ) );
        grid.addComponent( next2, 7, 4, 7, 4 );
        grid.setComponentAlignment( next2, Alignment.BOTTOM_RIGHT );

        setContent( grid );
    }


    private class NextButtonClickHandler implements Button.ClickListener
    {

        private final Wizard wizard;
        private final SetupType setupType;


        public NextButtonClickHandler( Wizard wizard, SetupType setupType )
        {
            this.wizard = wizard;
            this.setupType = setupType;
        }


        @Override
        public void buttonClick( Button.ClickEvent event )
        {
            wizard.init();
            wizard.getConfig().setSetupType( setupType );
            wizard.next();
        }


    }


}

