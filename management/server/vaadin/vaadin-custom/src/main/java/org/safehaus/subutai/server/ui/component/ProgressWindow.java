/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.server.ui.component;


import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import org.safehaus.subutai.core.tracker.api.Tracker;

import com.google.gwt.thirdparty.guava.common.base.Strings;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


public class ProgressWindow
{
    private Window window;
    private TextArea outputTxtArea;
    private Button ok;
    private Label indicator;
    private UUID trackID;
    private Tracker tracker;
    private String source;
    private volatile boolean track = true;
    private ExecutorService executor;


    public ProgressWindow( ExecutorService executor, Tracker tracker, UUID trackID, String source )
    {

        final VerticalLayout l = new VerticalLayout();
        window = new Window( "Operation progress", l );
        window.setImmediate( true );
        window.setModal( false );
        window.setResizable( true );
        window.center();
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
        outputTxtArea.setId( "outputTxtArea" );
        outputTxtArea.setRows( 13 );
        outputTxtArea.setColumns( 42 );
        outputTxtArea.setImmediate( true );
        outputTxtArea.setWordwrap( true );

        content.addComponent( outputTxtArea );

        ok = new Button( "Ok" );
        ok.setId( "btnOk" );
        ok.setStyleName( "default" );
        ok.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                //close window
                track = false;
                window.close();
            }
        } );

        indicator = new Label();
        indicator.setId( "indicator" );
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


    private synchronized void start()
    {

        showProgress();
        executor.execute( new Runnable()
        {

            public void run()
            {
                while ( track )
                {
                    TrackerOperationView po = tracker.getTrackerOperation( source, trackID );
                    if ( po != null )
                    {
                        setOutput( po.getDescription() + "\nState: " + po.getState() + "\nLogs:\n" + po.getLog() );

                        if ( po.getState() == OperationState.SUCCEEDED || po.getState() == OperationState.FAILED )
                        {
                            hideProgress( po );
                            break;
                        }
                    }
                    else
                    {
                        setOutput( "Product operation not found. Check logs" );

                        break;
                    }
                    try
                    {
                        Thread.sleep( 1000 );
                    }
                    catch ( InterruptedException ex )
                    {
                        break;
                    }
                }
            }
        } );
    }


    private void showProgress()
    {
        indicator.setVisible( true );
        ok.setEnabled( false );
    }


    private void setOutput( String output )
    {
        try
        {
//            VaadinSession.getCurrent().getLockInstance().lock();


            if ( !Strings.isNullOrEmpty( output ) )
            {
                outputTxtArea.setValue( output );
                outputTxtArea.setCursorPosition( outputTxtArea.getValue().length() - 1 );
            }
        }
        finally
        {
//            VaadinSession.getCurrent().getLockInstance().unlock();

        }
    }


    private void hideProgress( final TrackerOperationView to )
    {
        indicator.setVisible( false );
        ok.setEnabled( true );

        Notification.show( to.getState() + " " + to.getDescription(), to.getLog(), Notification.Type.WARNING_MESSAGE );
    }


    public Window getWindow()
    {
        return window;
    }
}
