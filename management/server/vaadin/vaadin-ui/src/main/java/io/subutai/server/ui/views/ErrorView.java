package io.subutai.server.ui.views;


import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import io.subutai.server.ui.MainUI;
import io.subutai.server.ui.util.HelpManager;


public class ErrorView extends Window

{
    private Label error;
    private HelpManager helpManager;
    private MainUI mainUI;
    String errorMessage;


    public ErrorView( MainUI mainUI, HelpManager helpManager)
    {
        setModal( true );
        this.mainUI = mainUI;
        this.helpManager = helpManager;

        buildView();
    }


    private void buildView()
    {
        center();

        final VerticalLayout layout = new VerticalLayout();

        if(helpManager!=null)
            helpManager.closeAll();

        layout.addStyleName( "login" );
        layout.addStyleName( "login-layout" );

        final CssLayout loginPanel = new CssLayout();
        loginPanel.addStyleName( "login-panel" );

        HorizontalLayout labels = new HorizontalLayout();
        labels.setWidth( "100%" );
        labels.setMargin( true );
        labels.addStyleName( "labels" );
        loginPanel.addComponent( labels );

        Label welcome = new Label( "Error:" );
        welcome.setSizeUndefined();
        welcome.addStyleName( "h4" );
        labels.addComponent( welcome );
        labels.setComponentAlignment( welcome, Alignment.MIDDLE_LEFT );

        HorizontalLayout fields = new HorizontalLayout();
        fields.setSpacing( true );
        fields.setMargin( true );
        fields.addStyleName( "fields" );


        final Button closeBt = new Button( "Close" );
        closeBt.addStyleName( "default" );
        fields.addComponent( closeBt );
        fields.setComponentAlignment( closeBt, Alignment.BOTTOM_LEFT );

        error = new Label( "", ContentMode.HTML );
        error.addStyleName( "error" );
        error.setSizeUndefined();
        error.addStyleName( "light" );
        // Add animation
        error.addStyleName( "v-animate-reveal" );
        error.setValue( errorMessage );
        loginPanel.addComponent( error );

        final ShortcutListener enter = new ShortcutListener( "Close", ShortcutAction.KeyCode.ENTER, null )
        {
            @Override
            public void handleAction( Object sender, Object target )
            {
                closeBt.click();
            }
        };

        closeBt.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {

                try
                {
                    close();
                }
                catch ( Exception e )
                {
                }
            }
        } );

        closeBt.addShortcutListener( enter );

        loginPanel.addComponent( fields );

        layout.addComponent( loginPanel );
        layout.setComponentAlignment( loginPanel, Alignment.MIDDLE_CENTER );
        setContent( layout );
    }


    public void setErrorMessage( String errorMessage)
    {
        error.setValue( errorMessage );
    }
}
