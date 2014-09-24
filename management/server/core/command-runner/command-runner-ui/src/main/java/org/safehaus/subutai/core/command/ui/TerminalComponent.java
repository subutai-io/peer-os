/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.command.ui;


import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.server.ui.component.AgentTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


/**
 * Command Runner UI - Terminal
 */
public class TerminalComponent extends CustomComponent implements Disposable
{
    private static final Logger LOG = LoggerFactory.getLogger( TerminalComponent.class.getName() );


    final CommandRunner commandRunner;
    final AgentManager agentManager;
    private AtomicInteger taskCount = new AtomicInteger();
    private ExecutorService executor;
    private AgentTree agentTree;
    private TerminalControlCssLayout commandOutputTxtArea;
    //
    private TextField programTxtFld, workDirTxtFld, timeoutTxtFld;
    private ComboBox requestTypeCombo;
    private Label indicator;
    private VerticalLayout controls;


    public TerminalComponent( final CommandRunner commandRunner, final AgentManager agentManager )
    {
        this.commandRunner = commandRunner;
        this.agentManager = agentManager;

        executor = Executors.newCachedThreadPool();

        setSizeFull();

        HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
        horizontalSplit.setSplitPosition( 20, Unit.PERCENTAGE );
        horizontalSplit.setSizeFull();

        agentTree = new AgentTree( agentManager );

        horizontalSplit.setFirstComponent( agentTree );

        HorizontalSplitPanel gridLayout = new HorizontalSplitPanel();
        gridLayout.setSizeFull();
        gridLayout.setSplitPosition( 80, Unit.PERCENTAGE );

        commandOutputTxtArea = new TerminalControlCssLayout( this );

        controls = new VerticalLayout();
        controls.setSpacing( true );
        controls.setMargin( true );

        initProgram();
        initCommand();
        initTimeout();
        initRequestType();
        initIndicator();

        gridLayout.setFirstComponent( commandOutputTxtArea );
        gridLayout.setSecondComponent( controls );

        horizontalSplit.setSecondComponent( gridLayout );
        setCompositionRoot( horizontalSplit );
    }


    private void initProgram()
    {
        Label programLbl = new Label( "Program" );
        programTxtFld = new TextField();
        programTxtFld.setId( "pwd" );
        programTxtFld.setValue( "pwd" );
        programTxtFld.setWidth( 200, Unit.PIXELS );
        controls.addComponent( programLbl );
        controls.addComponent( programTxtFld );
    }


    private void initCommand()
    {
        Label workDirLbl = new Label( "Cwd" );
        workDirTxtFld = new TextField();
        workDirTxtFld.setValue( "/" );
        controls.addComponent( workDirLbl );
        controls.addComponent( workDirTxtFld );
    }


    private void initTimeout()
    {
        Label timeoutLbl = new Label( "Timeout" );
        timeoutTxtFld = new TextField();
        timeoutTxtFld.setId( "timeout_txt" );
        timeoutTxtFld.setValue( "30" );
        timeoutTxtFld.setWidth( 30, Unit.PIXELS );
        controls.addComponent( timeoutLbl );
        controls.addComponent( timeoutTxtFld );
    }


    private void initRequestType()
    {
        Label requestTypeLabel = new Label( "Req Type" );
        controls.addComponent( requestTypeLabel );
        requestTypeCombo = new ComboBox( null,
                Arrays.asList( RequestType.EXECUTE_REQUEST, RequestType.TERMINATE_REQUEST, RequestType.PS_REQUEST ) );
        requestTypeCombo.setImmediate( true );
        requestTypeCombo.setTextInputAllowed( false );
        requestTypeCombo.setNullSelectionAllowed( false );
        requestTypeCombo.setValue( RequestType.EXECUTE_REQUEST );
        requestTypeCombo.setWidth( 200, Unit.PIXELS );
        controls.addComponent( requestTypeCombo );
    }


    private void initIndicator()
    {
        indicator = new Label();
        indicator.setId( "terminal_indicator" );
        indicator.setIcon( new ThemeResource( "img/spinner.gif" ) );
        indicator.setContentMode( ContentMode.HTML );
        indicator.setHeight( 11, Unit.PIXELS );
        indicator.setWidth( 50, Unit.PIXELS );
        indicator.setVisible( false );
        controls.addComponent( indicator );
    }


    public void sendCommand( String command )
    {
        Set<Agent> agents = checkAgents();
        if ( agents != null && validateInputs( command ) )
        {
            RequestBuilder requestBuilder = new RequestBuilder( command );

            if ( requestTypeCombo.getValue() == RequestType.TERMINATE_REQUEST )
            {
                requestBuilder.withPid( Integer.parseInt( programTxtFld.getValue() ) );
                requestBuilder.withType( RequestType.TERMINATE_REQUEST );
            }
            else if ( requestTypeCombo.getValue() == RequestType.PS_REQUEST )
            {
                requestBuilder.withType( RequestType.PS_REQUEST );
            }

            requestBuilder.withTimeout( Integer.parseInt( timeoutTxtFld.getValue() ) );
            requestBuilder.withCwd( workDirTxtFld.getValue() );

            createCommand( requestBuilder, agents );
        }
    }


    private Set<Agent> checkAgents()
    {
        Set<Agent> agents = agentTree.getSelectedAgents();
        if ( agents.isEmpty() )
        {
            agents = null;
            show( "Please, select nodes\\n" );
        }

        return agents;
    }


    private void show( String notification )
    {
        commandOutputTxtArea.setOutputPrompt( notification );
    }


    private boolean validateInputs( String command )
    {

        if ( Strings.isNullOrEmpty( command ) )
        {
            show( "Please, enter command\\n" );
            return false;
        }

        if ( Strings.isNullOrEmpty( timeoutTxtFld.getValue() ) || !StringUtil.isNumeric( timeoutTxtFld.getValue() ) )
        {
            show( "Please, enter integer timeout value\\n" );
            return false;
        }
        else
        {
            int timeout = Integer.parseInt( timeoutTxtFld.getValue() );
            if ( timeout <= 0 || timeout > Common.MAX_COMMAND_TIMEOUT_SEC )
            {
                show( "Please, enter timeout value between 0 and " + Common.MAX_COMMAND_TIMEOUT_SEC + "\\n" );
            }
        }

        if ( Strings.isNullOrEmpty( workDirTxtFld.getValue() ) )
        {
            show( "Please, enter working directory\\n" );
            return false;
        }

        return true;
    }


    private void createCommand( RequestBuilder requestBuilder, Set<Agent> agents )
    {
        indicator.setVisible( true );
        executor.execute(
                new ExecuteCommandTask( commandRunner.createCommand( requestBuilder, agents ), taskCount, indicator,
                        this ) );
    }


    public void dispose()
    {
        agentTree.dispose();
        executor.shutdown();
    }


    private static class ExecuteCommandTask implements Runnable
    {

        private final Command command;
        private final AtomicInteger taskCount;
        private final Label indicator;
        private final TerminalComponent form;

        private final StringBuilder output = new StringBuilder();


        private ExecuteCommandTask( final Command command, final AtomicInteger taskCount, final Label indicator,
                                    final TerminalComponent form )
        {
            this.command = command;
            this.taskCount = taskCount;
            this.indicator = indicator;
            this.form = form;
            taskCount.incrementAndGet();
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
                        StringBuilder out = new StringBuilder( "" );

                        if ( !Strings.isNullOrEmpty( response.getStdOut() ) )
                        {
                            out.append( response.getStdOut() );
                        }
                        if ( !Strings.isNullOrEmpty( response.getStdErr() ) )
                        {
                            out.append( response.getStdErr() );
                        }

                        output.append( out.toString() );
                    }
                } );
            }
            catch ( CommandException e )
            {
                LOG.error( "Error in ExecuteCommandTask", e );
                form.show( e.getMessage() );
            }

            taskCount.decrementAndGet();
            if ( taskCount.get() == 0 )
            {
                form.show( output.toString() );
                indicator.setVisible( false );
            }
        }
    }
}
