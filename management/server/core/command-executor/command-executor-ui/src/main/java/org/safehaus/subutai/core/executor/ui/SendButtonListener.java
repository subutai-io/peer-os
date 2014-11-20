package org.safehaus.subutai.core.executor.ui;


import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.command.RequestType;
import org.safehaus.subutai.common.command.Response;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.NumUtil;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.core.executor.api.CommandExecutor;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostState;
import org.safehaus.subutai.core.hostregistry.api.HostDisconnectedException;
import org.safehaus.subutai.core.hostregistry.api.HostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.vaadin.ui.Button;


/**
 * Handles send button click, executes commands and displays responses
 */
public class SendButtonListener implements Button.ClickListener
{
    private static final Logger LOG = LoggerFactory.getLogger( SendButtonListener.class.getName() );

    private final TerminalForm form;
    private final CommandExecutor commandExecutor;
    private final ExecutorService executor;
    private final HostRegistry hostRegistry;


    public SendButtonListener( final TerminalForm form, final CommandExecutor commandExecutor,
                               final ExecutorService executor, final HostRegistry hostRegistry )
    {

        Preconditions.checkNotNull( form );
        Preconditions.checkNotNull( commandExecutor );
        Preconditions.checkNotNull( executor );
        Preconditions.checkNotNull( hostRegistry );

        this.form = form;
        this.commandExecutor = commandExecutor;
        this.executor = executor;
        this.hostRegistry = hostRegistry;
    }


    @Override
    public void buttonClick( Button.ClickEvent event )
    {
        Set<HostInfo> hosts = form.getHostTree().getSelectedHosts();
        if ( hosts.isEmpty() )
        {
            form.addOutput( String.format( "Please, select hosts%n" ) );
        }
        else if ( Strings.isNullOrEmpty( form.getProgramTxtFld().getValue() ) )
        {
            form.addOutput( String.format( "Please, enter command%n" ) );
        }
        else
        {
            Set<HostInfo> connectedHosts = Sets.newHashSet();
            for ( final HostInfo hostInfo : hosts )
            {
                try
                {
                    if ( hostInfo instanceof ResourceHostInfo )
                    {
                        hostRegistry.getResourceHostInfoById( hostInfo.getId() );
                        connectedHosts.add( hostInfo );
                    }
                    else
                    {
                        ContainerHostInfo containerHostInfo = hostRegistry.getContainerHostInfoById( hostInfo.getId() );
                        if ( containerHostInfo.getStatus() != ContainerHostState.RUNNING )
                        {
                            form.addOutput( String.format( "Host %s is disconnected%n", hostInfo.getHostname() ) );
                        }
                        else
                        {
                            connectedHosts.add( hostInfo );
                        }
                    }
                }
                catch ( HostDisconnectedException e )
                {
                    LOG.warn( "Host not connected", e );
                    form.addOutput( String.format( "Host %s is disconnected%n", hostInfo.getHostname() ) );
                }
            }

            if ( !connectedHosts.isEmpty() )
            {
                executeCommand( connectedHosts );
            }
        }
    }


    private void executeCommand( Set<HostInfo> hosts )
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

            if ( !Strings.isNullOrEmpty( form.getWorkDirTxtFld().getValue() ) )
            {
                requestBuilder.withCwd( form.getWorkDirTxtFld().getValue() );
            }

            if ( !Strings.isNullOrEmpty( form.getRunAsTxtFld().getValue() ) )
            {
                requestBuilder.withRunAs( form.getRunAsTxtFld().getValue() );
            }

            int timeout = Integer.valueOf( form.getTimeoutTxtFld().getValue() );
            requestBuilder.withTimeout( timeout );

            executor.execute( new ExecuteCommandTask( commandExecutor, requestBuilder, hosts, form ) );
        }
    }


    private boolean checkRequest()
    {
        if ( form.getRequestTypeCombo().getValue() == RequestType.TERMINATE_REQUEST && !(
                StringUtil.isNumeric( form.getProgramTxtFld().getValue() )
                        && Integer.valueOf( form.getProgramTxtFld().getValue() ) > 0 ) )
        {

            form.addOutput( String.format( "Please, enter numeric PID greater than 0 to kill%n" ) );
            return false;
        }

        if ( !( StringUtil.isNumeric( form.getTimeoutTxtFld().getValue() ) && NumUtil
                .isIntBetween( Integer.valueOf( form.getTimeoutTxtFld().getValue() ), Common.MIN_COMMAND_TIMEOUT_SEC,
                        Common.MAX_COMMAND_TIMEOUT_SEC ) ) )
        {

            form.addOutput( String.format( "Timeout must be between %d and %d%n", Common.MIN_COMMAND_TIMEOUT_SEC,
                    Common.MAX_COMMAND_TIMEOUT_SEC ) );
            return false;
        }


        return true;
    }


    public static class ExecuteCommandTask implements Runnable
    {

        private final CommandExecutor commandExecutor;
        private final RequestBuilder requestBuilder;
        private final Set<HostInfo> hosts;
        private final TerminalForm form;


        public ExecuteCommandTask( CommandExecutor commandExecutor, RequestBuilder requestBuilder, Set<HostInfo> hosts,
                                   final TerminalForm form )
        {
            this.commandExecutor = commandExecutor;
            this.requestBuilder = requestBuilder;
            this.hosts = hosts;
            this.form = form;
            form.getIndicator().setVisible( true );
        }


        public void run()
        {
            try
            {
                for ( final HostInfo hostInfo : hosts )
                {
                    form.getTaskCount().incrementAndGet();
                    commandExecutor.executeAsync( hostInfo.getId(), requestBuilder, new CommandCallback()
                    {
                        @Override
                        public void onResponse( final Response response, final CommandResult commandResult )
                        {
                            displayResponse( hostInfo, response, commandResult );
                            if ( commandResult.hasCompleted() || commandResult.hasTimedOut() )
                            {
                                form.getTaskCount().decrementAndGet();

                                if ( form.getTaskCount().get() == 0 )
                                {
                                    form.getIndicator().setVisible( false );
                                }
                            }
                        }
                    } );
                }
            }
            catch ( CommandException e )
            {
                LOG.error( "Error in ExecuteCommandTask", e );
                form.addOutput( String.format( "%s%n", e.getMessage() ) );
            }
        }


        public void displayResponse( HostInfo hostInfo, Response response, CommandResult commandResult )
        {
            StringBuilder out = new StringBuilder();
            if ( !Strings.isNullOrEmpty( response.getStdOut() ) )
            {
                out.append( response.getStdOut() ).append( String.format( "%n" ) );
            }
            if ( !Strings.isNullOrEmpty( response.getStdErr() ) )
            {
                out.append( response.getStdErr() ).append( String.format( "%n" ) );
            }
            if ( commandResult.hasCompleted() )
            {
                if ( response.getExitCode() != 0 )
                {
                    out.append( "Exit code: " ).append( response.getExitCode() ).append( String.format( "%n%n" ) );
                }
            }
            else if ( commandResult.hasTimedOut() )
            {

                out.append( commandResult.getStatus() ).append( String.format( "%n%n" ) );
            }

            if ( out.length() > 0 )
            {
                form.addOutput( String.format( "%s [%d]:%n%s", hostInfo.getHostname(), response.getPid(), out ) );
            }
        }
    }
}
