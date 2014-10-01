package org.safehaus.subutai.core.environment.terminal.ui;


import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.enums.ResponseType;
import org.safehaus.subutai.common.protocol.ExecuteCommandMessage;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.NumUtil;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.core.environment.api.EnvironmentContainer;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
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
    private final EnvironmentManager environmentManager;
    private final ExecutorService executor;
    //    private final CommandRunner commandRunner;


    public SendButtonListener( final TerminalForm form, final EnvironmentManager environmentManager,
                               ExecutorService executor )
    {
        this.form = form;
        this.environmentManager = environmentManager;
        this.executor = executor;
        //        this.commandRunner = commandRunner;
    }


    @Override
    public void buttonClick( Button.ClickEvent event )
    {
        EnvironmentContainer container = form.environmentTree.getSelectedContainer();
        if ( container == null )
        {
            form.show( "Please, select node" );
        }
        else if ( form.programTxtFld.getValue() == null || Strings.isNullOrEmpty( form.programTxtFld.getValue() ) )
        {
            form.show( "Please, enter command" );
        }
        else
        {
            executeCommand( container );
        }
    }


    private void executeCommand( EnvironmentContainer container )
    {

        RequestBuilder requestBuilder = new RequestBuilder( form.programTxtFld.getValue() );

        if ( checkRequest( requestBuilder ) )
        {

            if ( form.requestTypeCombo.getValue() == RequestType.TERMINATE_REQUEST )
            {
                requestBuilder.withPid( Integer.valueOf( form.programTxtFld.getValue() ) );
                requestBuilder.withType( RequestType.TERMINATE_REQUEST );
            }
            else if ( form.requestTypeCombo.getValue() == RequestType.PS_REQUEST )
            {
                requestBuilder.withType( RequestType.PS_REQUEST );
            }

            if ( form.workDirTxtFld.getValue() != null && !Strings.isNullOrEmpty( form.workDirTxtFld.getValue() ) )
            {
                requestBuilder.withCwd( form.workDirTxtFld.getValue() );
            }

            int timeout = Integer.valueOf( form.timeoutTxtFld.getValue() );
            requestBuilder.withTimeout( timeout );

            form.indicator.setVisible( true );

            ExecuteCommandMessage ecm =
                    new ExecuteCommandMessage( container.getEnvironment().getUuid(), container.getPeerId(),
                            container.getAgentId(), form.programTxtFld.getValue(), RequestType.EXECUTE_REQUEST, timeout,
                            form.workDirTxtFld.getValue() );
            executor.execute( new ExecuteCommandTask( ecm, environmentManager, form ) );
        }
    }


    private boolean checkRequest( RequestBuilder requestBuilder )
    {
        if ( form.requestTypeCombo.getValue() == RequestType.TERMINATE_REQUEST && !(
                StringUtil.isNumeric( form.programTxtFld.getValue() )
                        && Integer.valueOf( form.programTxtFld.getValue() ) > 0 ) )
        {

            form.show( "Please, enter numeric PID greater than 0 to kill" );
            return false;
        }

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

        private final EnvironmentManager environmentManager;
        private final TerminalForm form;
        private final ExecuteCommandMessage ecm;


        private ExecuteCommandTask( ExecuteCommandMessage ecm, final EnvironmentManager environmentManager,
                                    final TerminalForm form )
        {
            this.environmentManager = environmentManager;
            this.form = form;
            this.ecm = ecm;
            form.taskCount.incrementAndGet();
        }


        public void run()
        {
            environmentManager.invoke( ecm, ecm.getTimeout() * 1000 );

            if ( ecm.isSuccess() )
            {
                ExecuteCommandMessage.ExecutionResult result =
                        ( ExecuteCommandMessage.ExecutionResult ) ecm.getResult();
                form.addOutput( result.getStdOut() );
                form.addOutput( result.getStdErr() );
                form.addOutput( "\n" );

                //                form.addOutput( ecm.getExitCode() );
            }
            else
            {
                form.addOutput( "Exception: " + ecm.getExceptionMessage() );

                LOG.warn( String.format( "Execute Command Message Response is null %s", ecm ) );
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
                //                Agent agent = agentManager.getAgentByUUID( response.getUuid() );
                //                StringBuilder host = new StringBuilder(
                //                        agent == null ? String.format( "Offline[%s]",
                // response.getUuid() ) : agent.getHostname() );
                //
                //                host.append( " [" ).append( response.getPid() ).append( "]" ).append( ":\n" );
                //                out.insert( 0, host );
                form.addOutput( out.toString() );
            }
        }
    }
}
