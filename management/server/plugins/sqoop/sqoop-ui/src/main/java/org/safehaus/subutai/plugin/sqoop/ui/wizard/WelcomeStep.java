package org.safehaus.subutai.plugin.sqoop.ui.wizard;


import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.plugin.sqoop.api.SetupType;
import org.safehaus.subutai.plugin.sqoop.ui.SqoopPortalModule;

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

        Label welcomeMsg = new Label( "<center><h2>Welcome to Sqoop Installation Wizard!</h2>" );
        welcomeMsg.setContentMode( ContentMode.HTML );
        grid.addComponent( welcomeMsg, 3, 1, 6, 2 );

        Label logoImg = new Label();
        logoImg.setIcon( new FileResource( FileUtil.getFile( SqoopPortalModule.MODULE_IMAGE, this ) ) );
        logoImg.setContentMode( ContentMode.HTML );
        logoImg.setHeight( 150, Unit.PIXELS );
        logoImg.setWidth( 150, Unit.PIXELS );
        grid.addComponent( logoImg, 1, 3, 2, 5 );

        Button next = new Button( "Start over-Hadoop installation" );
        next.setId( "sqoopStartOverHadoop" );
        next.addStyleName( "default" );
        next.addClickListener( new ClickListerner( wizard, SetupType.OVER_HADOOP ) );
        grid.addComponent( next, 4, 4, 4, 4 );
        grid.setComponentAlignment( next, Alignment.BOTTOM_RIGHT );

        Button next2 = new Button( "Start with-Hadoop installation" );
        next2.addStyleName( "default" );
        next2.setId( "sqoopStartWithHadoop" );
        next2.addClickListener( new ClickListerner( wizard, SetupType.WITH_HADOOP ) );
        grid.addComponent( next2, 5, 4, 5, 4 );
        grid.setComponentAlignment( next2, Alignment.BOTTOM_RIGHT );

        setContent( grid );
    }


    private class ClickListerner implements Button.ClickListener
    {

        final Wizard wizard;
        final SetupType type;


        public ClickListerner( Wizard wizard, SetupType type )
        {
            this.wizard = wizard;
            this.type = type;
        }


        @Override
        public void buttonClick( Button.ClickEvent event )
        {
            wizard.init();
            wizard.getConfig().setSetupType( type );
            wizard.next();
        }
    }
}
