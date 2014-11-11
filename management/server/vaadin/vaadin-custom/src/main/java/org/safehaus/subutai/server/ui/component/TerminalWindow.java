/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.server.ui.component;


import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.peer.api.ContainerHost;

import com.google.common.base.Strings;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;


/**
 * @author dilshat
 */
public class TerminalWindow
{

    private final Window window;
    private final TextArea commandOutputTxtArea;
    private volatile int taskCount = 0;
    private int commandTimeout = 30;


    public TerminalWindow( final Set<ContainerHost> containerHosts, int commandTimeout )
    {
        this( containerHosts );
        this.commandTimeout = commandTimeout;
    }


    public TerminalWindow( final Set<ContainerHost> containerHosts )
    {

        GridLayout grid = new GridLayout();
        grid.setColumns( 1 );
        grid.setRows( 11 );
        grid.setSizeFull();
        grid.setMargin( true );
        grid.setSpacing( true );

        window = new Window( String.format( "Shell" ), grid );
        window.setModal( true );
        window.setWidth( 600, Unit.PIXELS );
        window.setHeight( 420, Unit.PIXELS );

        commandOutputTxtArea = new TextArea( "Commands output" );
        commandOutputTxtArea.setRows( 15 );
        commandOutputTxtArea.setColumns( 40 );
        commandOutputTxtArea.setImmediate( true );
        commandOutputTxtArea.setWordwrap( true );

        HorizontalLayout controls = new HorizontalLayout();
        controls.setSpacing( true );

        Label lblCommand = new Label( "Command" );
        lblCommand.addStyleName( "dark" );

        final TextField txtCommand = new TextField();
        txtCommand.setWidth( 250, Unit.PIXELS );
        txtCommand.setValue( "pwd" );

        final Button clearBtn = new Button( "Clear" );
        clearBtn.addStyleName( "default" );

        final Button sendBtn = new Button( "Send" );
        sendBtn.addStyleName( "default" );

        final Label indicator = new Label();
        indicator.setIcon( new ThemeResource( "img/spinner.gif" ) );
        indicator.setContentMode( ContentMode.HTML );
        indicator.setHeight( 11, Unit.PIXELS );
        indicator.setWidth( 50, Unit.PIXELS );
        indicator.setVisible( false );

        controls.addComponent( lblCommand );
        controls.addComponent( txtCommand );
        controls.addComponent( clearBtn );
        controls.addComponent( sendBtn );
        controls.addComponent( indicator );

        grid.addComponent( commandOutputTxtArea, 0, 0, 0, 9 );
        grid.setComponentAlignment( commandOutputTxtArea, Alignment.TOP_CENTER );
        grid.addComponent( controls, 0, 10 );
        grid.setComponentAlignment( controls, Alignment.BOTTOM_CENTER );

        txtCommand.addShortcutListener( new ShortcutListener( "Shortcut Name", ShortcutAction.KeyCode.ENTER, null )
        {
            @Override
            public void handleAction( Object sender, Object target )
            {
                sendBtn.click();
            }
        } );
        sendBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( !Strings.isNullOrEmpty( txtCommand.getValue() ) )
                {
                    indicator.setVisible( true );
                    taskCount++;
                    /*final Command command = commandRunner
                            .createCommand( new RequestBuilder( txtCommand.getValue() ).withTimeout( commandTimeout ),
                                    agents );*/
                    for ( ContainerHost containerHost : containerHosts )
                    {
                        try
                        {
                            CommandResult result = containerHost.execute(
                                    new RequestBuilder( txtCommand.getValue() ).withTimeout( commandTimeout ) );
                            StringBuilder out = new StringBuilder( containerHost.getId().toString() ).append( ":\n" );
                            if ( !Strings.isNullOrEmpty( result.getStdOut() ) )
                            {
                                out.append( result.getStdOut() ).append( '\n' );
                            }
                            if ( !Strings.isNullOrEmpty( result.getStdErr() ) )
                            {
                                out.append( result.getStdErr() ).append( '\n' );
                            }
                            if ( result.hasCompleted() )
                            {
                                out.append( "Exit code: " ).append( result.getExitCode() ).append( "\n\n" );
                            }
                            else
                            {
                                out.append( "Command has not completed" ).append( "\n\n" );
                            }
                            addOutput( out.toString() );
                        }
                        catch ( CommandException e )
                        {

                        }
                    }
                    /*executor.execute( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            commandRunner.runCommand( command, new CommandCallback()
                            {

                                @Override
                                public void onResponse( Response response, AgentResult agentResult, Command command )
                                {
                                    Agent agent = agentManager.getAgentByUUID( agentResult.getAgentUUID() );
                                    String host = agent == null ? String.format( "Offline[%s]", response.getUuid() ) :
                                                  agent.getHostname();
                                    StringBuilder out = new StringBuilder( host ).append( ":\n" );
                                    if ( !Strings.isNullOrEmpty( response.getStdOut() ) )
                                    {
                                        out.append( response.getStdOut() ).append( '\n' );
                                    }
                                    if ( !Strings.isNullOrEmpty( response.getStdErr() ) )
                                    {
                                        out.append( response.getStdErr() ).append( '\n' );
                                    }
                                    if ( response.isFinal() )
                                    {
                                        if ( response.getType() == ResponseType.EXECUTE_RESPONSE_DONE )
                                        {
                                            out.append( "Exit code: " ).append( response.getExitCode() )
                                               .append( "\n\n" );
                                        }
                                        else
                                        {
                                            out.append( "Command timed out" ).append( "\n\n" );
                                        }
                                    }
                                    addOutput( out.toString() );
                                }
                            } );
                            taskCount--;
                            if ( taskCount == 0 )
                            {
                                indicator.setVisible( false );
                            }
                        }
                    } );*/
                }
                else
                {
                    addOutput( "Please enter command to run" );
                }
            }
        } );
        clearBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                commandOutputTxtArea.setValue( "" );
            }
        } );
    }


    private void addOutput( String output )
    {
        if ( !Strings.isNullOrEmpty( output ) )
        {
            commandOutputTxtArea.setValue( String.format( "%s%s", commandOutputTxtArea.getValue(), output ) );
            commandOutputTxtArea.setCursorPosition( commandOutputTxtArea.getValue().length() - 1 );
        }
    }


    public Window getWindow()
    {
        return window;
    }
}
