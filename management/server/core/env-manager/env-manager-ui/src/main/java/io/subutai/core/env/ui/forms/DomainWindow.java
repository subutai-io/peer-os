package io.subutai.core.env.ui.forms;


import com.google.common.base.Strings;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.settings.Common;
import io.subutai.core.env.api.EnvironmentManager;
import io.subutai.core.env.api.exception.EnvironmentManagerException;
import io.subutai.server.ui.component.ConfirmationDialog;


public class DomainWindow extends Window
{


    public DomainWindow( final Environment environment, final EnvironmentManager environmentManager )
    {

        setCaption( "Environment domain" );
        setWidth( "300px" );
        setHeight( "120px" );
        setModal( true );
        setClosable( true );

        GridLayout content = new GridLayout( 2, 1 );
        content.setSizeFull();
        content.setMargin( true );
        content.setSpacing( true );


        final TextField domainTxt = new TextField();
        try
        {
            String currentDomain = environmentManager.getDomain( environment.getId() );
            domainTxt.setValue( currentDomain == null ? "" : currentDomain );
        }
        catch ( EnvironmentNotFoundException | EnvironmentManagerException e )
        {
            Notification.show( "Error obtaining current domain", e.getMessage(), Notification.Type.ERROR_MESSAGE );
            close();
        }

        Button domainBtn = new Button( "Assign" );
        domainBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                final String newDomain = domainTxt.getValue();
                //remove environment domain
                if ( Strings.isNullOrEmpty( domainTxt.getValue() ) )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you really want to remove domain %s from the environment %s?", newDomain,
                                    environment.getName() ), "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( final Button.ClickEvent event )
                        {
                            //remove domain from the environment
                            try
                            {
                                environmentManager.removeDomain( environment.getId(), true );
                                Notification.show( "Please, wait..." );
                                close();
                            }
                            catch ( EnvironmentNotFoundException | EnvironmentModificationException e )
                            {
                                Notification.show( "Error removing domain", e.getMessage(),
                                        Notification.Type.ERROR_MESSAGE );
                            }
                        }
                    } );

                    getUI().addWindow( alert.getAlert() );
                }
                //set environment domain
                else
                {
                    if ( newDomain.matches( Common.HOSTNAME_REGEX ) )
                    {
                        ConfirmationDialog alert = new ConfirmationDialog(
                                String.format( "Do you really want to assign domain %s to the environment %s?",
                                        newDomain, environment.getName() ), "Yes", "No" );

                        alert.getOk().addClickListener( new Button.ClickListener()
                        {
                            @Override
                            public void buttonClick( final Button.ClickEvent event )
                            {
                                //assign domain to the environment
                                try
                                {
                                    environmentManager.assignDomain( environment.getId(), newDomain, true );
                                    Notification.show( "Please, wait..." );
                                    close();
                                }
                                catch ( EnvironmentNotFoundException | EnvironmentModificationException e )
                                {
                                    Notification.show( "Error assigning domain", e.getMessage(),
                                            Notification.Type.ERROR_MESSAGE );
                                }
                            }
                        } );
                        getUI().addWindow( alert.getAlert() );
                    }
                    else
                    {
                        Notification.show( String.format( "Domain %s is invalid", newDomain ) );
                    }
                }
            }
        } );

        content.addComponent( domainTxt );
        content.addComponent( domainBtn );

        setContent( content );
    }
}
