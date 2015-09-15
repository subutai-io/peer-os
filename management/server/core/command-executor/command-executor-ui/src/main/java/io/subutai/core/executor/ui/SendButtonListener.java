package io.subutai.core.executor.ui;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.vaadin.ui.Button;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.command.RequestType;
import io.subutai.common.command.Response;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInfo;
import io.subutai.common.settings.Common;
import io.subutai.common.util.NumUtil;
import io.subutai.common.util.StringUtil;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.hostregistry.api.ContainerHostInfo;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.hostregistry.api.ResourceHostInfo;


/**
 * Handles send button click, executes commands and displays responses
 */
public class SendButtonListener implements Button.ClickListener, CommandCallback
{
    private static final Logger LOG = LoggerFactory.getLogger( SendButtonListener.class.getName() );

    private final TerminalForm form;
    private final CommandExecutor commandExecutor;
    private final HostRegistry hostRegistry;


    public SendButtonListener( final TerminalForm form, final CommandExecutor commandExecutor,
                               final HostRegistry hostRegistry )
    {
        Preconditions.checkNotNull( form );
        Preconditions.checkNotNull( commandExecutor );
        Preconditions.checkNotNull( hostRegistry );

        this.form = form;
        this.commandExecutor = commandExecutor;
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
                addIfHostConnected( connectedHosts, hostInfo );
            }

            if ( !connectedHosts.isEmpty() )
            {
                executeCommand( connectedHosts );
            }
        }
    }


    protected void addIfHostConnected( Set<HostInfo> connectedHosts, HostInfo hostInfo )
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


    protected void executeCommand( Set<HostInfo> hosts )
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

            if ( form.getDaemonChk().getValue() )
            {
                requestBuilder.daemon();
            }

            execute( requestBuilder, hosts );
        }
    }


    protected boolean checkRequest()
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

        if ( form.getRequestTypeCombo().getValue() == RequestType.PS_REQUEST )
        {
            Set<HostInfo> hosts = form.getHostTree().getSelectedHosts();

            for ( HostInfo hostInfo : hosts )
            {
                if ( hostInfo instanceof ContainerHostInfo )
                {
                    form.addOutput( String.format(
                            "You could not send PS_REQUEST to container host. Please select only resource hosts.%n" ) );
                    return false;
                }
            }
        }
        return true;
    }


    protected void execute( RequestBuilder requestBuilder, Set<HostInfo> hosts )
    {
        try
        {
            form.getIndicator().setVisible( true );

            for ( final HostInfo hostInfo : hosts )
            {
                form.getTaskCount().incrementAndGet();
                commandExecutor.executeAsync( hostInfo.getId(), requestBuilder, this );
            }
        }
        catch ( CommandException e )
        {
            LOG.error( "Error in ExecuteCommandTask", e );
            form.addOutput( String.format( "%s%n", e.getMessage() ) );
        }
    }


    protected HostInfo getHostById( String hostId )
    {
        try
        {
            return hostRegistry.getHostInfoById( hostId );
        }
        catch ( HostDisconnectedException e )
        {
            LOG.debug( "ignore", e );

            return null;
        }
    }


    @Override
    public void onResponse( final Response response, final CommandResult commandResult )
    {

        if ( form.isAttached() )
        {
            displayResponse( getHostById( response.getId() ), response, commandResult );
            if ( commandResult.hasCompleted() || commandResult.hasTimedOut() )
            {
                form.getTaskCount().decrementAndGet();

                if ( form.getTaskCount().get() == 0 )
                {
                    form.getIndicator().setVisible( false );
                }
            }
        }
    }


    protected void displayResponse( HostInfo hostInfo, Response response, CommandResult commandResult )
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
            if ( response.getExitCode() != null )
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
            form.addOutput( String.format( "%s [%d]:%n%s", hostInfo == null ? response.getId() : hostInfo.getHostname(),
                    response.getPid(), out ) );
        }
    }
}
