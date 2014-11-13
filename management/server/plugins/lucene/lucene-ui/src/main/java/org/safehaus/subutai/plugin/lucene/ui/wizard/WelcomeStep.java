package org.safehaus.subutai.plugin.lucene.ui.wizard;


import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.plugin.lucene.api.SetupType;
import org.safehaus.subutai.plugin.lucene.ui.LucenePortalModule;

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

        Label welcomeMsg = new Label( "<center><h2>Welcome to Lucene Installation Wizard!</h2>" );
        welcomeMsg.setContentMode( ContentMode.HTML );
        grid.addComponent( welcomeMsg, 3, 1, 6, 2 );

        Label logoImg = new Label();
        logoImg.setIcon( new FileResource( FileUtil.getFile( LucenePortalModule.MODULE_IMAGE, this ) ) );
        logoImg.setContentMode( ContentMode.HTML );
        logoImg.setHeight( 200, Unit.PIXELS );
        logoImg.setWidth( 180, Unit.PIXELS );
        grid.addComponent( logoImg, 1, 3, 2, 5 );

        Button next = new Button( "Start over-Hadoop installation" );
        next.setId( "LuceneOverHadoop" );
        next.addStyleName( "default" );
        //		next.setWidth(100, Unit.PIXELS);
        grid.addComponent( next, 4, 4, 4, 4 );
        grid.setComponentAlignment( next, Alignment.BOTTOM_RIGHT );

        next.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                clickHandler( wizard, SetupType.OVER_HADOOP );
            }
        } );

        /*Button next2 = new Button( "Start with-Hadoop installation" );
        next2.setId( "LuceneWithHadoop" );
        next2.setStyleName( "default" );
        next2.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                clickHandler( wizard, SetupType.WITH_HADOOP );
            }
        } );
        grid.addComponent( next2, 5, 4, 5, 4 );
        grid.setComponentAlignment( next2, Alignment.BOTTOM_RIGHT );*/

        setContent( grid );
    }


    private void clickHandler( Wizard wizard, SetupType type )
    {
        wizard.init();
        wizard.getConfig().setSetupType( type );
        wizard.next();
    }
}