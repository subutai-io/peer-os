package org.safehaus.subutai.core.command.ui.old;


import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.command.RequestType;
import org.safehaus.subutai.common.enums.ResponseType;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.NumUtil;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.vaadin.ui.Button;


/**
 * Handles send button click, executes commands and displays responses
 */
public class SendButtonListener implements Button.ClickListener
{
    private static final Logger LOG = LoggerFactory.getLogger( SendButtonListener.class.getName() );

    private final TerminalForm form;
    private final AgentManager agentManager;
    private final CommandRunner commandRunner;
    private final ExecutorService executor;


    public SendButtonListener( final TerminalForm form, final AgentManager agentManager,
                               final CommandRunner commandRunner, final ExecutorService executor )
    {

        Preconditions.checkNotNull( form, "Terminal form is null" );
        Preconditions.checkNotNull( agentManager, "Agent Manager is null" );
        Preconditions.checkNotNull( commandRunner, "Command Dispatcher is null" );
        Preconditions.checkNotNull( executor, "Executor is null" );

        this.form = form;
        this.agentManager = agentManager;
        this.commandRunner = commandRunner;
        this.executor = executor;
    }


    @Override
    public void buttonClick( Button.ClickEvent event )
    {
        Set<Agent> agents = form.getAgentTree().getSelectedAgents();
        if ( agents.isEmpty() )
        {
            form.show( "Please, select nodes" );
        }
        else if ( Strings.isNullOrEmpty( form.getProgramTxtFld().getValue() ) )
        {
            form.show( "Please, enter command" );
        }
        else
        {
            executeCommand( agents );
        }
    }


    private void executeCommand( Set<Agent> agents )
    {

        RequestBuilder requestBuilder = new RequestBuilder( form.getProgramTxtFld().getValue() );

        if ( checkRequest() )
        {

            if ( form.getRequestTypeCombo().getValue() == RequestType.TERMINATE_REQUEST )
            {
                requestBuilder.withPid( Integer.valueOf( form.getProgramTxtFld().getValue() ) );
                requestBuilder.withType( RequestType.TERMINATE_REQUEST );
            }
            else if ( form.getRequestTypeCombo().getValue() == RequestType.PS_REQUEST )
            {
                requestBuilder.withType( RequestType.PS_REQUEST );
            }

            if ( form.getWorkDirTxtFld().getValue() != null && !Strings
                    .isNullOrEmpty( form.getWorkDirTxtFld().getValue() ) )
            {
                requestBuilder.withCwd( form.getWorkDirTxtFld().getValue() );
            }

            int timeout = Integer.valueOf( form.getTimeoutTxtFld().getValue() );
            requestBuilder.withTimeout( timeout );

            form.getIndicator().setVisible( true );

            executor.execute(
                    new ExecuteCommandTask( commandRunner.createCommand( requestBuilder, agents ), agentManager,
                            form ) );
        }
    }


    private boolean checkRequest()
    {
        if ( form.getRequestTypeCombo().getValue() == RequestType.TERMINATE_REQUEST && !(
                StringUtil.isNumeric( form.getProgramTxtFld().getValue() )
                        && Integer.valueOf( form.getProgramTxtFld().getValue() ) > 0 ) )
        {

            form.show( "Please, enter numeric PID greater than 0 to kill" );
            return false;
        }

        if ( !( StringUtil.isNumeric( form.getTimeoutTxtFld().getValue() ) && NumUtil
                .isIntBetween( Integer.valueOf( form.getTimeoutTxtFld().getValue() ), Common.MIN_COMMAND_TIMEOUT_SEC,
                        Common.MAX_COMMAND_TIMEOUT_SEC ) ) )
        {

            form.show( String.format( "Timeout must be between %d and %d", Common.MIN_COMMAND_TIMEOUT_SEC,
                    Common.MAX_COMMAND_TIMEOUT_SEC ) );
            return false;
        }


        return true;
    }


    public static class ExecuteCommandTask implements Runnable
    {

        private final Command command;
        private final AgentManager agentManager;
        private final TerminalForm form;


        public ExecuteCommandTask( final Command command, final AgentManager agentManager, final TerminalForm form )
        {
            this.command = command;
            this.agentManager = agentManager;
            this.form = form;
            form.getTaskCount().incrementAndGet();
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

            form.getTaskCount().decrementAndGet();
            if ( form.getTaskCount().get() == 0 )
            {
                form.getIndicator().setVisible( false );
            }
        }


        public void displayResponse( Response response )
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
                Agent agent = agentManager.getAgentByUUID( response.getUuid() );
                StringBuilder host = new StringBuilder(
                        agent == null ? String.format( "Offline[%s]", response.getUuid() ) : agent.getHostname() );

                host.append( " [" ).append( response.getPid() ).append( "]" ).append( ":\n" );
                out.insert( 0, host );
                form.addOutput( out.toString() );
            }
        }
    }
}
