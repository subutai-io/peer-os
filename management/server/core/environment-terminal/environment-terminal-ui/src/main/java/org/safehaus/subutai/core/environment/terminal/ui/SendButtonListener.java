package org.safehaus.subutai.core.environment.terminal.ui;


import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.Response;
import org.safehaus.subutai.common.command.ResponseType;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.NumUtil;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
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


    public SendButtonListener( final TerminalForm form, ExecutorService executor )
    {
        this.form = form;
        this.executor = executor;
    }


    @Override
    public void buttonClick( Button.ClickEvent event )
    {
        Set<ContainerHost> containers = form.environmentTree.getSelectedContainers();
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


    private void executeCommand( Set<ContainerHost> containers )
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

            for ( ContainerHost host : containers )
            {
                executor.execute( new ExecuteCommandTask( form, host, requestBuilder ) );
            }
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
        private Host host;
        private RequestBuilder requestBuilder;


        private ExecuteCommandTask( TerminalForm form, Host host, RequestBuilder requestBuilder )
        {
            this.form = form;
            this.host = host;
            this.requestBuilder = requestBuilder;
            form.taskCount.incrementAndGet();
        }


        public void run()
        {

            try
            {
                host.execute( requestBuilder, new CommandCallback()
                {
                    @Override
                    public void onResponse( final Response response, final CommandResult commandResult )
                    {
                        displayResponse( response, commandResult );
                    }
                } );
            }
            catch ( CommandException e )
            {
                LOG.error( "Error in ExecuteCommandTask", e );
                form.addOutput( e.getMessage() + "\n" );
            }
            finally
            {
                form.taskCount.decrementAndGet();
                if ( form.taskCount.get() == 0 )
                {
                    form.indicator.setVisible( false );
                }
            }
        }


        private void displayResponse( Response response, CommandResult commandResult )
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
            if ( commandResult.hasCompleted() || commandResult.hasTimedOut() )
            {
                if ( response.getType() == ResponseType.EXECUTE_RESPONSE && response.getExitCode() != 0 )
                {
                    out.append( "Exit code: " ).append( response.getExitCode() ).append( "\n\n" );
                }
                else if ( response.getType() != ResponseType.EXECUTE_RESPONSE )
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
