package org.safehaus.subutai.core.environment.ui.manage;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerPortalModule;
import org.safehaus.subutai.core.environment.ui.executor.BuildProcessExecutionEvent;
import org.safehaus.subutai.core.environment.ui.executor.BuildProcessExecutionEventType;
import org.safehaus.subutai.core.environment.ui.executor.BuildProcessExecutionListener;
import org.safehaus.subutai.core.environment.ui.executor.BuildProcessExecutor;
import org.safehaus.subutai.core.environment.ui.executor.BuildProcessExecutorImpl;
import org.safehaus.subutai.core.environment.ui.executor.CloneCommandFactory;
import org.safehaus.subutai.core.environment.ui.text.EnvAnswer;
import org.safehaus.subutai.core.environment.ui.window.EnvironmentBuildProcessDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings("serial")
public class EnvironmentsBuildProcessForm implements BuildProcessExecutionListener
{

    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentsBuildProcessForm.class.getName() );
    private static final String OK_ICON_SOURCE = "img/ok.png";
    private static final String ERROR_ICON_SOURCE = "img/cancel.png";
    private static final String LOAD_ICON_SOURCE = "img/spinner.gif";
//    private static final String IMG = "img/spinner.gif";
    private AtomicInteger errorProcessed = null;
    private VerticalLayout contentRoot;
    private Table environmentsTable;
    private EnvironmentManagerPortalModule managerUI;


    public EnvironmentsBuildProcessForm( final EnvironmentManagerPortalModule managerUI )
    {
        this.managerUI = managerUI;

        contentRoot = new VerticalLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );

        environmentsTable = createTable( "Environments Build Process", 300 );

        Button getEnvironmentsButton = new Button( "View" );
        getEnvironmentsButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                updateTableData();
            }
        } );
        contentRoot.addComponent( getEnvironmentsButton );
        contentRoot.addComponent( environmentsTable );
    }


    private Table createTable( String caption, int size )
    {
        Table table = new Table( caption );
        table.addContainerProperty( "Name", UUID.class, null );
        table.addContainerProperty( "Status", Embedded.class, null );
        table.addContainerProperty( "Info", Button.class, null );
        table.addContainerProperty( "Action", Button.class, null );
        table.addContainerProperty( "Destroy", Button.class, null );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();
        return table;
    }


    private void updateTableData()
    {
        environmentsTable.removeAllItems();
        List<EnvironmentBuildProcess> environmentBuildProcessList =
                managerUI.getEnvironmentManager().getBuildProcesses();
        if ( !environmentBuildProcessList.isEmpty() )
        {
            for ( final EnvironmentBuildProcess environmentBuildProcess : environmentBuildProcessList )
            {
                Button viewEnvironmentInfoButton = new Button( "Info" );
                viewEnvironmentInfoButton.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent )
                    {
                        EnvironmentBuildProcessDetails detailsWindow =
                                new EnvironmentBuildProcessDetails( "Environment details" );
                        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                        String json = gson.toJson( environmentBuildProcess, EnvironmentBuildProcess.class );
                        detailsWindow.setContent( json );
                        contentRoot.getUI().addWindow( detailsWindow );
                        detailsWindow.setVisible( true );
                    }
                } );

                Button processButton = null;
//                Embedded progressIcon = null;
                Button destroyButton = null;

                switch ( environmentBuildProcess.getProcessStatusEnum() )
                {
                    case NEW_PROCESS:
                    {
                        processButton = new Button( "Build" );
//                        progressIcon = new Embedded( "", new ThemeResource( IMG ) );
//                        progressIcon.setVisible( false );

                        processButton.addClickListener( new Button.ClickListener()
                        {
                            @Override
                            public void buttonClick( final Button.ClickEvent clickEvent )
                            {
                                startBuildProcess( environmentBuildProcess );
                            }
                        } );

                        destroyButton = new Button( "Destroy" );
                        destroyButton.addClickListener( new Button.ClickListener()
                        {
                            @Override
                            public void buttonClick( final Button.ClickEvent clickEvent )
                            {
                                destroyBuildProcess( environmentBuildProcess );
                            }
                        } );


                        break;
                    }
                    case IN_PROGRESS:
                    {
                        processButton = new Button( "Terminate" );
//                        progressIcon = new Embedded( "", new ThemeResource( IMG ) );
//                        progressIcon.setVisible( true );
                        processButton.addClickListener( new Button.ClickListener()
                        {
                            @Override
                            public void buttonClick( final Button.ClickEvent clickEvent )
                            {
                                terminateBuildProcess( environmentBuildProcess );
                            }
                        } );
                        break;
                    }
                    case FAILED:
                    {
                        processButton = new Button( "Destroy" );
//                        progressIcon = new Embedded( "", new ThemeResource( "img/cancel.png" ) );
//                        progressIcon.setVisible( true );
                        processButton.addClickListener( new Button.ClickListener()
                        {
                            @Override
                            public void buttonClick( final Button.ClickEvent clickEvent )
                            {
                                destroyBuildProcess( environmentBuildProcess );
                            }
                        } );
                        break;
                    }
                    case SUCCESSFUL:
                    {
                        processButton = new Button( "Configure" );
//                        progressIcon = new Embedded( "", new ThemeResource( "img/ok.png" ) );
//                        progressIcon.setVisible( true );
                        processButton.addClickListener( new Button.ClickListener()
                        {
                            @Override
                            public void buttonClick( final Button.ClickEvent clickEvent )
                            {
                                configureEnvironment( environmentBuildProcess );
                            }
                        } );
                        break;
                    }
                    default:
                    {
                        break;
                    }
                }
                environmentsTable.addItem( new Object[] {
                        environmentBuildProcess.getUuid(), null, viewEnvironmentInfoButton, processButton,
                        destroyButton
                }, environmentBuildProcess.getUuid().toString() );
            }
        }
        else
        {
            Notification.show( EnvAnswer.NO_BUILD_PROCESS.getAnswer() );
        }
        environmentsTable.refreshRowCache();
    }


    private void configureEnvironment( final EnvironmentBuildProcess environmentBuildProcess )
    {
        //TODO: configure code
    }


    private void terminateBuildProcess( final EnvironmentBuildProcess environmentBuildProcess )
    {

        //TODO:terminate code
    }


    private void destroyBuildProcess( final EnvironmentBuildProcess environmentBuildProcess )
    {
        managerUI.getEnvironmentManager().deleteBuildProcess( environmentBuildProcess );
    }


    private void startBuildProcess( final EnvironmentBuildProcess environmentBuildProcess )
    {

        errorProcessed = new AtomicInteger( 0 );

        BuildProcessExecutor buildProcessExecutor = new BuildProcessExecutorImpl(environmentBuildProcess);
        buildProcessExecutor.addListener( this );
        ExecutorService executor = Executors.newSingleThreadExecutor();
        buildProcessExecutor.execute( executor,
                new CloneCommandFactory( managerUI.getEnvironmentManager(), environmentBuildProcess ) );
        executor.shutdown();
    }


    @Override
    public void onExecutionEvent( final BuildProcessExecutionEvent event )
    {
        LOG.info( event.toString() );
        updateEnvironmentsTableStatus( event );
    }


    private void updateEnvironmentsTableStatus( final BuildProcessExecutionEvent event )
    {
        contentRoot.getUI().access( new Runnable()
        {
            @Override
            public void run()
            {
                Item row = environmentsTable.getItem( event.getName() );
                if ( row != null )
                {
                    Property p = row.getItemProperty( "Status" );
                    if ( BuildProcessExecutionEventType.START.equals( event.getEventType() ) )
                    {
                        p.setValue( new Embedded( "", new ThemeResource( LOAD_ICON_SOURCE ) ) );
                    }
                    else if ( BuildProcessExecutionEventType.SUCCESS.equals( event.getEventType() ) )
                    {
                        p.setValue( new Embedded( "", new ThemeResource( OK_ICON_SOURCE ) ) );
                    }
                    else if ( BuildProcessExecutionEventType.FAIL.equals( event.getEventType() ) )
                    {
                        p.setValue( new Embedded( "", new ThemeResource( ERROR_ICON_SOURCE ) ) );

                        errorProcessed.incrementAndGet();
                    }
                }


                if ( errorProcessed.intValue() == 0 )
                {
                    Notification.show( EnvAnswer.SUCCESS.getAnswer() );
                }
                else
                {
                    Notification.show( EnvAnswer.FAIL.getAnswer() );
                }
            }
        } );
    }


    public VerticalLayout getContentRoot()
    {
        return this.contentRoot;
    }
}
