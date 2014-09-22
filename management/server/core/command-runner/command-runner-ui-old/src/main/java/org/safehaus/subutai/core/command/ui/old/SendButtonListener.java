package org.safehaus.subutai.core.command.ui.old;


import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.enums.ResponseType;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;

import com.google.common.base.Strings;
import com.vaadin.ui.Button;


/**
 * Handles send button click, executes commands and displays responses
 */
public class SendButtonListener implements Button.ClickListener
{
    private static final Logger LOG = Logger.getLogger( SendButtonListener.class.getName() );

    private final TerminalForm form;
    private final AgentManager agentManager;
    private final CommandDispatcher commandDispatcher;
    private final ExecutorService executor;


    public SendButtonListener( final TerminalForm form, final AgentManager agentManager,
                               final CommandDispatcher commandDispatcher, final ExecutorService executor )
    {
        this.form = form;
        this.agentManager = agentManager;
        this.commandDispatcher = commandDispatcher;
        this.executor = executor;
    }


    @Override
    public void buttonClick( Button.ClickEvent event )
    {
        Set<Agent> agents = form.agentTree.getSelectedAgents();
        if ( agents.isEmpty() )
        {
            form.show( "Please, select nodes" );
        }
        else if ( form.programTxtFld.getValue() == null || Strings.isNullOrEmpty( form.programTxtFld.getValue() ) )
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

        RequestBuilder requestBuilder = new RequestBuilder( form.programTxtFld.getValue() );

        if ( form.requestTypeCombo.getValue() == RequestType.TERMINATE_REQUEST )
        {
            if ( StringUtil.isNumeric( form.programTxtFld.getValue() )
                    && Integer.valueOf( form.programTxtFld.getValue() ) > 0 )
            {
                requestBuilder.withPid( Integer.valueOf( form.programTxtFld.getValue() ) );
                requestBuilder.withType( RequestType.TERMINATE_REQUEST );
            }
            else
            {
                form.show( "Please, enter numeric PID greater than 0 to kill" );
                return;
            }
        }
        else if ( form.requestTypeCombo.getValue() == RequestType.PS_REQUEST )
        {
            requestBuilder.withType( RequestType.PS_REQUEST );
        }

        if ( form.timeoutTxtFld.getValue() != null && StringUtil.isNumeric( form.timeoutTxtFld.getValue() ) )
        {
            int timeout = Integer.valueOf( form.timeoutTxtFld.getValue() );
            if ( timeout > 0 && timeout <= Common.MAX_COMMAND_TIMEOUT_SEC )
            {
                requestBuilder.withTimeout( timeout );
            }
        }

        if ( form.workDirTxtFld.getValue() != null && !Strings.isNullOrEmpty( form.workDirTxtFld.getValue() ) )
        {
            requestBuilder.withCwd( form.workDirTxtFld.getValue() );
        }
        final Command command = commandDispatcher.createCommand( requestBuilder, agents );
        form.indicator.setVisible( true );
        form.taskCount.incrementAndGet();

        executor.execute( new ExecuteCommandTask( command, agentManager, form ) );
    }


    private static class ExecuteCommandTask implements Runnable
    {

        private final Command command;
        private final AgentManager agentManager;
        private final TerminalForm form;


        private ExecuteCommandTask( final Command command, final AgentManager agentManager, final TerminalForm form )
        {
            this.command = command;
            this.agentManager = agentManager;
            this.form = form;
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
                LOG.log( Level.SEVERE, "Error in ExecuteCommandTask", e );
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
