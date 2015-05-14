/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.executor.ui;


import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.common.command.RequestType;
import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.core.executor.api.CommandExecutor;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.server.ui.component.HostTree;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;


/**
 * Terminal Form
 */
public class TerminalForm extends CustomComponent implements Disposable
{

    private final TextField programTxtFld;
    private final TextField timeoutTxtFld;
    private final TextField workDirTxtFld;
    private final TextField runAsTxtFld;
    private final CheckBox daemonChk;
    private final ComboBox requestTypeCombo;
    private final Label indicator;
    private final AtomicInteger taskCount = new AtomicInteger();
    protected HostTree hostTree;
    protected TextArea commandOutputTxtArea;


    public TerminalForm( final CommandExecutor commandExecutor, final HostRegistry hostRegistry )
    {

        Preconditions.checkNotNull( commandExecutor );
        Preconditions.checkNotNull( hostRegistry );

        setSizeFull();

        HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
        horizontalSplit.setSplitPosition( 200, Unit.PIXELS );
        hostTree = new HostTree( hostRegistry, false );
        horizontalSplit.setFirstComponent( hostTree );

        GridLayout grid = new GridLayout( 20, 11 );
        grid.setSizeFull();
        grid.setMargin( true );
        grid.setSpacing( true );
        commandOutputTxtArea = new TextArea( "Commands output" );
        commandOutputTxtArea.setSizeFull();
        commandOutputTxtArea.setImmediate( true );
        commandOutputTxtArea.setWordwrap( false );
        grid.addComponent( commandOutputTxtArea, 0, 0, 19, 8 );

        HorizontalLayout controls = new HorizontalLayout();
        controls.setSpacing( true );
        Label programLbl = new Label( "Command" );
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
        Label runAsLbl = new Label( "Run As" );
        runAsTxtFld = new TextField();
        runAsTxtFld.setValue( "root" );
        controls.addComponent( runAsLbl );
        controls.addComponent( runAsTxtFld );
        Label timeoutLbl = new Label( "Timeout" );
        timeoutTxtFld = new TextField();
        timeoutTxtFld.setValue( "30" );
        timeoutTxtFld.setWidth( 30, Unit.PIXELS );
        controls.addComponent( timeoutLbl );
        controls.addComponent( timeoutTxtFld );
        Label requestTypeLabel = new Label( "Req Type" );
        controls.addComponent( requestTypeLabel );
        requestTypeCombo = new ComboBox( null,
                Arrays.asList( RequestType.EXECUTE_REQUEST, RequestType.TERMINATE_REQUEST, RequestType.PS_REQUEST ) );
        requestTypeCombo.setImmediate( true );
        requestTypeCombo.setTextInputAllowed( false );
        requestTypeCombo.setNullSelectionAllowed( false );
        requestTypeCombo.setValue( RequestType.EXECUTE_REQUEST );
        requestTypeCombo.setWidth( 150, Unit.PIXELS );
        controls.addComponent( requestTypeCombo );
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
                hostTree.filterContainerHostsByTag( filterTxt.getValue() );
            }
        } );


        grid.addComponent( filterControls, 0, 10, 19, 10 );

        horizontalSplit.setSecondComponent( grid );
        setCompositionRoot( horizontalSplit );

        programTxtFld.addShortcutListener( new ShortcutListener( "Shortcut Name", ShortcutAction.KeyCode.ENTER, null )
        {
            @Override
            public void handleAction( Object sender, Object target )
            {
                sendBtn.click();
            }
        } );

        sendBtn.addClickListener( new SendButtonListener( this, commandExecutor, hostRegistry ) );

        clearBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                commandOutputTxtArea.setValue( "" );
            }
        } );
    }


    public CheckBox getDaemonChk()
    {
        return daemonChk;
    }


    protected HostTree getHostTree()
    {
        return hostTree;
    }


    protected TextField getProgramTxtFld()
    {
        return programTxtFld;
    }


    protected TextField getTimeoutTxtFld()
    {
        return timeoutTxtFld;
    }


    protected TextField getWorkDirTxtFld()
    {
        return workDirTxtFld;
    }


    public TextField getRunAsTxtFld()
    {
        return runAsTxtFld;
    }


    protected ComboBox getRequestTypeCombo()
    {
        return requestTypeCombo;
    }


    protected Label getIndicator()
    {
        return indicator;
    }


    protected AtomicInteger getTaskCount()
    {
        return taskCount;
    }


    public void addOutput( final String output )
    {
        if ( commandOutputTxtArea.isAttached() )
        {
            commandOutputTxtArea.getUI().access( new Runnable()
            {
                @Override
                public void run()
                {
                    if ( !Strings.isNullOrEmpty( output ) )
                    {
                        commandOutputTxtArea
                                .setValue( String.format( "%s%s", commandOutputTxtArea.getValue(), output ) );
                        commandOutputTxtArea.setCursorPosition( commandOutputTxtArea.getValue().length() - 1 );
                    }
                }
            } );
        }
    }


    public void dispose()
    {
        hostTree.dispose();
    }
}
