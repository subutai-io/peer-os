package org.safehaus.subutai.core.environment.terminal.ui;


import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.common.enums.ResponseType;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.NumUtil;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.core.command.api.command.CommandException;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;
import org.safehaus.subutai.core.environment.api.EnvironmentContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.vaadin.ui.Button;


/**
 * Handles send button click, executes commands and displays responses
 */
public class SendButtonListener implements Button.ClickListener
{
    private static final Logger LOG = LoggerFactory.getLogger( SendButtonListener.class.getName() );

    private final TerminalForm form;
    private final ExecutorService executor;
    private final CommandDispatcher commandDispatcher;


    public SendButtonListener( final CommandDispatcher commandDispatcher, final TerminalForm form,
                               ExecutorService executor )
    {
        this.form = form;
        this.executor = executor;
        this.commandDispatcher = commandDispatcher;
    }


    @Override
    public void buttonClick( Button.ClickEvent event )
    {
        Set<EnvironmentContainer> containers = form.environmentTree.getSelectedContainers();
        if ( CollectionUtil.isCollectionEmpty( containers ) )
        {
            form.show( "Please, select container(s)" );
        }
        else if ( form.programTxtFld.getValue() == null || Strings.isNullOrEmpty( form.programTxtFld.getValue() ) )
        {
            form.show( "Please, enter command" );
        }
        else
        {
            executeCommand( containers );
        }
    }


    private void executeCommand( Set<EnvironmentContainer> containers )
    {

        RequestBuilder requestBuilder = new RequestBuilder( form.programTxtFld.getValue() );

        if ( checkRequest() )
        {

            if ( form.workDirTxtFld.getValue() != null && !Strings.isNullOrEmpty( form.workDirTxtFld.getValue() ) )
            {
                requestBuilder.withCwd( form.workDirTxtFld.getValue() );
            }

            int timeout = Integer.valueOf( form.timeoutTxtFld.getValue() );
            requestBuilder.withTimeout( timeout );

            form.indicator.setVisible( true );

            Set<Container> containerSet = new HashSet<>();
            containerSet.addAll( containers );
            Command command = commandDispatcher.createContainerCommand( requestBuilder, containerSet );
            executor.execute( new ExecuteCommandTask( form, command ) );
        }
    }


    private boolean checkRequest()
    {

        if ( !( StringUtil.isNumeric( form.timeoutTxtFld.getValue() ) && NumUtil
                .isIntBetween( Integer.valueOf( form.timeoutTxtFld.getValue() ), Common.MIN_COMMAND_TIMEOUT_SEC,
                        Common.MAX_COMMAND_TIMEOUT_SEC ) ) )
        {

            form.show( String.format( "Timeout must be between %d and %d", Common.MIN_COMMAND_TIMEOUT_SEC,
                    Common.MAX_COMMAND_TIMEOUT_SEC ) );
            return false;
        }


        return true;
    }


    private static class ExecuteCommandTask implements Runnable
    {

        private final TerminalForm form;
        private final Command command;


        private ExecuteCommandTask( TerminalForm form, Command command )
        {
            this.form = form;
            this.command = command;
            form.taskCount.incrementAndGet();
        }


        public void run()
        {
            try
            {

                command.execute( new CommandCallback()
                {

                    @Override
                    public void onResponse( Response response, AgentResult agentResult, Command command )
                    {
                        displayResponse( response );
                    }
                } );
            }
            catch ( CommandException e )
            {
                LOG.error( "Error in ExecuteCommandTask", e );
                form.show( e.getMessage() );
            }

            form.taskCount.decrementAndGet();
            if ( form.taskCount.get() == 0 )
            {
                form.indicator.setVisible( false );
            }
        }


        private void displayResponse( Response response )
        {
            StringBuilder out = new StringBuilder();
            if ( !Strings.isNullOrEmpty( response.getStdOut() ) )
            {
                out.append( response.getStdOut() ).append( "\n" );
            }
            if ( !Strings.isNullOrEmpty( response.getStdErr() ) )
            {
                out.append( response.getStdErr() ).append( "\n" );
            }
            if ( response.isFinal() )
            {
                if ( response.getType() == ResponseType.EXECUTE_RESPONSE_DONE && response.getExitCode() != 0 )
                {
                    out.append( "Exit code: " ).append( response.getExitCode() ).append( "\n\n" );
                }
                else if ( response.getType() != ResponseType.EXECUTE_RESPONSE_DONE )
                {
                    out.append( response.getType() ).append( "\n\n" );
                }
            }

            if ( out.length() > 0 )
            {
                form.addOutput( out.toString() );
            }
        }
    }
}
