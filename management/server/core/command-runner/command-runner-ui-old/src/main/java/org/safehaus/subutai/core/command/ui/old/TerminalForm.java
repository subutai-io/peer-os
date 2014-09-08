/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.command.ui.old;


import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.server.ui.component.AgentTree;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.enums.ResponseType;
import org.safehaus.subutai.common.settings.Common;

import com.google.common.base.Strings;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;


/**
 * Command Runner UI - Terminal
 */
public class TerminalForm extends CustomComponent implements Disposable {

    private final AgentTree agentTree;
    private final TextArea commandOutputTxtArea;
    private volatile int taskCount = 0;
    private ExecutorService executor;


    public TerminalForm( final CommandRunner commandRunner, final AgentManager agentManager ) {
        setSizeFull();

        executor = Executors.newCachedThreadPool();

        HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
        horizontalSplit.setSplitPosition( 200, Unit.PIXELS );
        agentTree = new AgentTree( agentManager );
        horizontalSplit.setFirstComponent( agentTree );

        GridLayout grid = new GridLayout( 20, 10 );
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
        Label programLbl = new Label( "Program" );
        final TextField programTxtFld = new TextField();
        programTxtFld.setValue( "pwd" );
        programTxtFld.setWidth( 300, Unit.PIXELS );
        controls.addComponent( programLbl );
        controls.addComponent( programTxtFld );
        Label workDirLbl = new Label( "Cwd" );
        final TextField workDirTxtFld = new TextField();
        workDirTxtFld.setValue( "/" );
        controls.addComponent( workDirLbl );
        controls.addComponent( workDirTxtFld );
        Label timeoutLbl = new Label( "Timeout" );
        final TextField timeoutTxtFld = new TextField();
        timeoutTxtFld.setValue( "30" );
        timeoutTxtFld.setWidth( 30, Unit.PIXELS );
        controls.addComponent( timeoutLbl );
        controls.addComponent( timeoutTxtFld );
        Label requestTypeLabel = new Label( "Req Type" );
        controls.addComponent( requestTypeLabel );
        final ComboBox requestTypeCombo = new ComboBox( null,
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
        final Label indicator = new Label();
        indicator.setId( "terminal_indicator" );
        indicator.setIcon( new ThemeResource( "img/spinner.gif" ) );
        indicator.setContentMode( ContentMode.HTML );
        indicator.setHeight( 11, Unit.PIXELS );
        indicator.setWidth( 50, Unit.PIXELS );
        indicator.setVisible( false );
        controls.addComponent( indicator );

        grid.addComponent( controls, 0, 9, 19, 9 );

        horizontalSplit.setSecondComponent( grid );
        setCompositionRoot( horizontalSplit );

        programTxtFld.addShortcutListener( new ShortcutListener( "Shortcut Name", ShortcutAction.KeyCode.ENTER, null ) {
            @Override
            public void handleAction( Object sender, Object target ) {
                sendBtn.click();
            }
        } );
        sendBtn.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent event ) {
                Set<Agent> agents = agentTree.getSelectedAgents();
                if ( agents.isEmpty() ) {
                    show( "Please, select nodes" );
                }
                else if ( programTxtFld.getValue() == null || Strings.isNullOrEmpty( programTxtFld.getValue() ) ) {
                    show( "Please, enter command" );
                }
                else {

                    RequestBuilder requestBuilder = new RequestBuilder( programTxtFld.getValue() );

                    if ( requestTypeCombo.getValue() == RequestType.TERMINATE_REQUEST ) {
                        if ( StringUtil.isNumeric( programTxtFld.getValue() )
                                && Integer.valueOf( programTxtFld.getValue() ) > 0 ) {
                            requestBuilder.withPid( Integer.valueOf( programTxtFld.getValue() ) );
                            requestBuilder.withType( RequestType.TERMINATE_REQUEST );
                        }
                        else {
                            show( "Please, enter numeric PID greater than 0 to kill" );
                            return;
                        }
                    }
                    else if ( requestTypeCombo.getValue() == RequestType.PS_REQUEST ) {
                        requestBuilder.withType( RequestType.PS_REQUEST );
                    }

                    if ( timeoutTxtFld.getValue() != null && StringUtil.isNumeric( timeoutTxtFld.getValue() ) ) {
                        int timeout = Integer.valueOf( timeoutTxtFld.getValue() );
                        if ( timeout > 0 && timeout <= Common.MAX_COMMAND_TIMEOUT_SEC ) {
                            requestBuilder.withTimeout( timeout );
                        }
                    }

                    if ( workDirTxtFld.getValue() != null && !Strings.isNullOrEmpty( workDirTxtFld.getValue() ) ) {
                        requestBuilder.withCwd( workDirTxtFld.getValue() );
                    }
                    final Command command = commandRunner.createCommand( requestBuilder, agents );
                    indicator.setVisible( true );
                    taskCount++;
                    executor.execute( new Runnable() {

                        public void run() {
                            commandRunner.runCommand( command, new CommandCallback() {

                                @Override
                                public void onResponse( Response response, AgentResult agentResult, Command command ) {
                                    Agent agent = agentManager.getAgentByUUID( response.getUuid() );
                                    String host = agent == null ? String.format( "Offline[%s]", response.getUuid() ) :
                                                  agent.getHostname();
                                    StringBuilder out =
                                            new StringBuilder( host ).append( " [" ).append( response.getPid() )
                                                                     .append( "]" ).append( ":\n" );
                                    if ( !Strings.isNullOrEmpty( response.getStdOut() ) ) {
                                        out.append( response.getStdOut() ).append( "\n" );
                                    }
                                    if ( !Strings.isNullOrEmpty( response.getStdErr() ) ) {
                                        out.append( response.getStdErr() ).append( "\n" );
                                    }
                                    if ( response.isFinal() ) {
                                        if ( response.getType() == ResponseType.EXECUTE_RESPONSE_DONE ) {
                                            out.append( "Exit code: " ).append( response.getExitCode() )
                                               .append( "\n\n" );
                                        }
                                        else {
                                            out.append( response.getType() ).append( "\n\n" );
                                        }
                                    }
                                    addOutput( out.toString() );
                                }
                            } );

                            taskCount--;
                            if ( taskCount == 0 ) {
                                indicator.setVisible( false );
                            }
                        }
                    } );
                }
            }
        } );
        clearBtn.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent event ) {
                commandOutputTxtArea.setValue( "" );
            }
        } );
    }


    private void show( String msg ) {
        Notification.show( msg );
    }


    private void addOutput( String output ) {
        if ( !Strings.isNullOrEmpty( output ) ) {
            commandOutputTxtArea.setValue( String.format( "%s%s", commandOutputTxtArea.getValue(), output ) );
            commandOutputTxtArea.setCursorPosition( commandOutputTxtArea.getValue().length() - 1 );
        }
    }


    public void dispose() {
        agentTree.dispose();
        executor.shutdown();
    }
}
