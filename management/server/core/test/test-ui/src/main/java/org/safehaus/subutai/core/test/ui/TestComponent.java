package org.safehaus.subutai.core.test.ui;


import org.safehaus.subutai.core.test.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.log4j.MDC;

import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;


public class TestComponent extends CustomComponent
{
    private static Logger LOG = LoggerFactory.getLogger( TestComponent.class.getName() );


    public TestComponent( final Test test )
    {

        VerticalLayout layout = new VerticalLayout();


        Button logBtn = new Button( "Log user" );
        logBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                test.logUsername();
            }
        } );

        Button showBtn = new Button( "Show user" );
        showBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                Notification.show( "USER >>>" + test.getUserName() );
            }
        } );

        Button loginWithTokenBtn = new Button( "Login with token" );
        loginWithTokenBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                Notification.show( test.loginWithToken( "karaf" ).toString() );
            }
        } );

        Button execBtn = new Button( "Exec" );
        execBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                String username = test.getUserName();
                LOG.error( "Setting MDC >>> " + username );

                MDC.put( "test", username );
                test.testExecutor();
            }
        } );

        layout.addComponent( logBtn );
        layout.addComponent( showBtn );
        layout.addComponent( loginWithTokenBtn );
        layout.addComponent( execBtn );


        setCompositionRoot( layout );
    }
}
