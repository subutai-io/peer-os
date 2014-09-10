/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.server.ui.component;


import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;

import com.google.common.base.Strings;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


/**
 * @author dilshat
 */
public class ProgressWindow {
    private Window window;
    private TextArea outputTxtArea;
    private Button ok;
    private Label indicator;
    private UUID trackID;
    private Tracker tracker;
    private String source;
    private volatile boolean track = true;
    private ExecutorService executor;


    public ProgressWindow( ExecutorService executor, Tracker tracker, UUID trackID, String source ) {

        final VerticalLayout l = new VerticalLayout();
        window = new Window( "Operation progress", l );
        window.setImmediate( true );
        window.setModal( true );
        window.setWidth( 650, Sizeable.Unit.PIXELS );

        this.trackID = trackID;
        this.tracker = tracker;
        this.source = source;
        this.executor = executor;

        GridLayout content = new GridLayout( 1, 2 );
        content.setSizeFull();
        content.setMargin( true );
        content.setSpacing( true );

        outputTxtArea = new TextArea( "Operation output" );
        outputTxtArea.setRows( 13 );
        outputTxtArea.setColumns( 42 );
        outputTxtArea.setImmediate( true );
        outputTxtArea.setWordwrap( true );

        content.addComponent( outputTxtArea );

        ok = new Button( "Ok" );
        ok.setStyleName( "default" );
        ok.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                //close window
                track = false;
                window.close();
            }
        } );

        indicator = new Label();
        indicator.setIcon( new ThemeResource( "img/spinner.gif" ) );
        indicator.setContentMode( ContentMode.HTML );
        indicator.setHeight( 11, Sizeable.Unit.PIXELS );
        indicator.setWidth( 50, Sizeable.Unit.PIXELS );
        indicator.setVisible( false );

        HorizontalLayout bottomContent = new HorizontalLayout();
        bottomContent.addComponent( indicator );
        bottomContent.setComponentAlignment( indicator, Alignment.MIDDLE_RIGHT );
        bottomContent.addComponent( ok );

        content.addComponent( bottomContent );
        content.setComponentAlignment( bottomContent, Alignment.MIDDLE_RIGHT );

        l.addComponent( content );
        start();
    }


    private void start() {

        showProgress();
        window.getUI().access( new Runnable() {

            public void run() {
                while ( track ) {
                    ProductOperationView po = tracker.getProductOperation( source, trackID );
                    if ( po != null ) {
                        setOutput( po.getDescription() + "\nState: " + po.getState() + "\nLogs:\n" + po.getLog() );

                        if ( po.getState() == ProductOperationState.SUCCEEDED
                                || po.getState() == ProductOperationState.FAILED ) {
                            hideProgress();
                            break;
                        }
                    }
                    else {
                        setOutput( "Product operation not found. Check logs" );

                        break;
                    }
                    try {
                        Thread.sleep( 1000 );
                    }
                    catch ( InterruptedException ex ) {
                        break;
                    }
                }
            }
        } );
    }


    private void showProgress() {
        indicator.setVisible( true );
        ok.setEnabled( false );
    }


    private void setOutput( String output ) {
        if ( !Strings.isNullOrEmpty( output ) ) {
            outputTxtArea.setValue( output );
            outputTxtArea.setCursorPosition( outputTxtArea.getValue().length() - 1 );
        }
    }


    private void hideProgress() {
        indicator.setVisible( false );
        ok.setEnabled( true );
    }


    public Window getWindow() {
        return window;
    }
}
