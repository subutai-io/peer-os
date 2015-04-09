package org.safehaus.subutai.core.environment.terminal.ui;


import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.common.mdc.SubutaiExecutors;
import org.safehaus.subutai.core.env.api.EnvironmentManager;

import com.google.common.base.Strings;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;


/**
 * Environment Terminal
 */
public class TerminalForm extends CustomComponent
{
    protected final EnvironmentTree environmentTree;
    protected final TextField programTxtFld;
    protected final TextField timeoutTxtFld;
    protected final TextField workDirTxtFld;
    protected final CheckBox daemonChk;

    protected final Label indicator;
    private final TextArea commandOutputTxtArea;
    protected AtomicInteger taskCount = new AtomicInteger();
    private ExecutorService executor;


    public TerminalForm( final EnvironmentManager environmentManager, final Date updateDate )
    {
        setSizeFull();


        HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
        horizontalSplit.setSplitPosition( 200, Unit.PIXELS );
        environmentTree = new EnvironmentTree( environmentManager, updateDate );
        horizontalSplit.setFirstComponent( environmentTree );

        GridLayout grid = new GridLayout( 20, 11 );
        grid.setSizeFull();
        grid.setMargin( true );
        grid.setSpacing( true );

        horizontalSplit.setSecondComponent( grid );

        commandOutputTxtArea = new TextArea( "Commands output" );
        commandOutputTxtArea.setSizeFull();
        commandOutputTxtArea.setImmediate( true );
        commandOutputTxtArea.setWordwrap( false );
        grid.addComponent( commandOutputTxtArea, 0, 0, 19, 8 );

        HorizontalLayout controls = new HorizontalLayout();
        controls.setSpacing( true );
        Label programLbl = new Label( "Program" );
        programTxtFld = new TextField();
        programTxtFld.setValue( "pwd" );
        programTxtFld.setWidth( 300, Unit.PIXELS );
        controls.addComponent( programLbl );
        controls.addComponent( programTxtFld );
        Label workDirLbl = new Label( "Cwd" );
        workDirTxtFld = new TextField();
        workDirTxtFld.setValue( "/" );
        controls.addComponent( workDirLbl );
        controls.addComponent( workDirTxtFld );
        Label timeoutLbl = new Label( "Timeout" );
        timeoutTxtFld = new TextField();
        timeoutTxtFld.setValue( "30" );
        timeoutTxtFld.setWidth( 30, Unit.PIXELS );
        controls.addComponent( timeoutLbl );
        controls.addComponent( timeoutTxtFld );
        Button clearBtn = new Button( "Clear" );
        controls.addComponent( clearBtn );
        final Button sendBtn = new Button( "Send" );
        controls.addComponent( sendBtn );
        daemonChk = new CheckBox( "Daemon" );
        controls.addComponent( daemonChk );
        indicator = new Label();
        indicator.setId( "terminal_indicator" );
        indicator.setIcon( new ThemeResource( "img/spinner.gif" ) );
        indicator.setContentMode( ContentMode.HTML );
        indicator.setHeight( 11, Unit.PIXELS );
        indicator.setWidth( 50, Unit.PIXELS );
        indicator.setVisible( false );
        controls.addComponent( indicator );

        grid.addComponent( controls, 0, 9, 19, 9 );

        HorizontalLayout filterControls = new HorizontalLayout();
        filterControls.setSpacing( true );
        Label filterLbl = new Label( "Tag" );
        final TextField filterTxt = new TextField();
        Button filterBtn = new Button( "Filter" );
        filterControls.addComponent( filterLbl );
        filterControls.addComponent( filterTxt );
        filterControls.addComponent( filterBtn );

        filterBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                environmentTree.filterContainerHostsByTag( filterTxt.getValue() );
            }
        } );

        programTxtFld.addShortcutListener( new ShortcutListener( "Shortcut Name", ShortcutAction.KeyCode.ENTER, null )
        {
            @Override
            public void handleAction( Object sender, Object target )
            {
                sendBtn.click();
            }
        } );

        final SendButtonListener sendButtonListener = new SendButtonListener( this );
        sendBtn.addClickListener( sendButtonListener );

        clearBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                commandOutputTxtArea.setValue( "" );
            }
        } );

        grid.addComponent( filterControls, 0, 10, 19, 10 );


        setCompositionRoot( horizontalSplit );

        addDetachListener( new DetachListener()
        {
            @Override
            public void detach( final DetachEvent event )
            {
                executor.shutdown();
            }
        } );

        addAttachListener( new AttachListener()
        {
            @Override
            public void attach( final AttachEvent event )
            {
                executor = SubutaiExecutors.newCachedThreadPool();
                sendButtonListener.setExecutor( executor );
            }
        } );
    }


    protected void show( String msg )
    {
        Notification.show( msg );
    }


    protected void addOutput( String output )
    {
        if ( !Strings.isNullOrEmpty( output ) )
        {
            commandOutputTxtArea.setValue( String.format( "%s%s", commandOutputTxtArea.getValue(), output ) );
            commandOutputTxtArea.setCursorPosition( commandOutputTxtArea.getValue().length() - 1 );
        }
    }
}
