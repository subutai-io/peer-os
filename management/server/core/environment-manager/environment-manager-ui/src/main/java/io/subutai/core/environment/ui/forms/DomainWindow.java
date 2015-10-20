package io.subutai.core.environment.ui.forms;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.google.common.base.Strings;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Window;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentModificationException;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.settings.Common;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.server.ui.component.ConfirmationDialog;


public class DomainWindow extends Window
{

    private CheckBox sslCheck;
    private String sslPath = null;


    public DomainWindow( final Environment environment, final EnvironmentManager environmentManager )
    {

        setCaption( "Environment domain" );
        setWidth( "400px" );
        setHeight( "300px" );
        setModal( true );
        setClosable( true );

        GridLayout content = new GridLayout( 2, 4 );
        content.setSizeFull();
        content.setMargin( true );
        content.setSpacing( true );


        final TextField domainTxt = new TextField();
        try
        {
            String currentDomain = environmentManager.getEnvironmentDomain( environment.getId() );
            domainTxt.setValue( currentDomain == null ? "" : currentDomain );
        }
        catch ( EnvironmentNotFoundException | EnvironmentManagerException e )
        {
            Notification.show( "Error obtaining current domain", e.getMessage(), Notification.Type.ERROR_MESSAGE );
            close();
        }

        final ComboBox loadBalanceStrategyCombo = new ComboBox();
        loadBalanceStrategyCombo.setNullSelectionAllowed( false );
        loadBalanceStrategyCombo.setTextInputAllowed( false );
        for ( final DomainLoadBalanceStrategy domainLoadBalanceStrategy : DomainLoadBalanceStrategy.values() )
        {
            loadBalanceStrategyCombo.addItem( domainLoadBalanceStrategy );
        }
        loadBalanceStrategyCombo.setValue( DomainLoadBalanceStrategy.ROUND_ROBIN );

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
                                environmentManager.removeEnvironmentDomain( environment.getId() );
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
                                    environmentManager.assignEnvironmentDomain( environment.getId(), newDomain,
                                            ( DomainLoadBalanceStrategy ) loadBalanceStrategyCombo.getValue(),
                                            sslPath );
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

        CertificateUploader certificateUploader = new CertificateUploader();
        Upload upload = new Upload( "SSL certificate", certificateUploader );
        upload.addSucceededListener( certificateUploader );

        content.addComponent( new Label( "Load-balance" ), 0, 0 );
        content.addComponent( loadBalanceStrategyCombo, 1, 0 );
        content.addComponent( new Label( "Domain" ), 0, 1 );
        content.addComponent( domainTxt, 1, 1 );
        content.addComponent( upload, 0, 2, 1, 2 );
        sslCheck = new CheckBox( "SSL cert" );
        sslCheck.setEnabled( false );
        sslCheck.setValue( false );
        content.addComponent( sslCheck, 0, 3 );
        content.addComponent( domainBtn, 1, 3 );

        setContent( content );
    }


    class CertificateUploader implements Upload.Receiver, Upload.SucceededListener
    {
        public File file;


        public OutputStream receiveUpload( String filename, String mimeType )
        {
            // Create upload stream
            FileOutputStream fos; // Stream to write to
            try
            {
                // Open the file for writing.
                file = new File( String.format( "%s/%s", System.getProperty( "java.io.tmpdir" ), filename ) );
                fos = new FileOutputStream( file );
            }
            catch ( final java.io.FileNotFoundException e )
            {
                new Notification( "Could not open file<br/>", e.getMessage(), Notification.Type.ERROR_MESSAGE )
                        .show( Page.getCurrent() );
                return null;
            }
            return fos; // Return the output stream to write to
        }


        public void uploadSucceeded( Upload.SucceededEvent event )
        {
            // Set SSL file path variable
            sslPath = String.format( "%s/%s", System.getProperty( "java.io.tmpdir" ), event.getFilename() );
            sslCheck.setValue( true );
        }
    }
}
