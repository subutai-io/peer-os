package io.subutai.core.environment.ui.forms;


import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Window;

import io.subutai.common.environment.ContainerHostNotFoundException;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.peer.ContainerHost;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;


public class ContainerDomainWindow extends Window
{
    public ContainerDomainWindow( final ContainerHost containerHost, final Environment environment,
                                  final EnvironmentManager environmentManager )
    {
        setCaption( "Container domain" );
        setWidth( "250px" );
        setHeight( "80px" );
        setModal( true );
        setClosable( true );

        HorizontalLayout content = new HorizontalLayout();
        content.setSizeFull();
        content.setMargin( true );


        final CheckBox isContainerInDomainChk = new CheckBox( "Is container in domain" );
        isContainerInDomainChk.setImmediate( true );

        try
        {
            isContainerInDomainChk.setValue(
                    environmentManager.isContainerInEnvironmentDomain( containerHost.getId(), environment.getId() ) );
        }
        catch ( EnvironmentNotFoundException | EnvironmentManagerException e )
        {
            Notification.show( "Error obtaining status of container in domain", e.getMessage(),
                    Notification.Type.ERROR_MESSAGE );
            close();
        }

        isContainerInDomainChk.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( final Property.ValueChangeEvent event )
            {
                if ( isContainerInDomainChk.getValue() )
                {
                    //include container to environment domain
                    isContainerInDomainChk.setEnabled( false );
                    Notification.show( "Please, wait..." );
                    getUI().access( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                environmentManager
                                        .addContainerToEnvironmentDomain( containerHost.getId(), environment.getId() );
                            }
                            catch ( EnvironmentNotFoundException | ContainerHostNotFoundException |
                                    EnvironmentModificationException e )
                            {
                                Notification.show( "Error adding container to domain", e.getMessage(),
                                        Notification.Type.ERROR_MESSAGE );
                                close();
                            }


                            isContainerInDomainChk.setEnabled( true );
                        }
                    } );
                }
                else
                {
                    //exclude container from environment domain
                    isContainerInDomainChk.setEnabled( false );
                    Notification.show( "Please, wait..." );
                    getUI().access( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                environmentManager.removeContainerFromEnvironmentDomain( containerHost.getId(),
                                        environment.getId() );
                            }
                            catch ( EnvironmentModificationException | ContainerHostNotFoundException |
                                    EnvironmentNotFoundException e )
                            {
                                Notification.show( "Error removing container from domain", e.getMessage(),
                                        Notification.Type.ERROR_MESSAGE );
                                close();
                            }

                            isContainerInDomainChk.setEnabled( true );
                        }
                    } );
                }
            }
        } );

        content.addComponent( isContainerInDomainChk );
        setContent( content );
    }
}
