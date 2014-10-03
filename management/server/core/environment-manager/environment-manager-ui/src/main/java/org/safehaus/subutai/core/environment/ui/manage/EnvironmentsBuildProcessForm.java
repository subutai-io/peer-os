package org.safehaus.subutai.core.environment.ui.manage;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.helper.ProcessStatusEnum;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerPortalModule;
import org.safehaus.subutai.core.environment.ui.executor.BuildCommandFactory;
import org.safehaus.subutai.core.environment.ui.executor.BuildProcessExecutionEvent;
import org.safehaus.subutai.core.environment.ui.executor.BuildProcessExecutionEventType;
import org.safehaus.subutai.core.environment.ui.executor.BuildProcessExecutionListener;
import org.safehaus.subutai.core.environment.ui.executor.BuildProcessExecutor;
import org.safehaus.subutai.core.environment.ui.executor.BuildProcessExecutorImpl;
import org.safehaus.subutai.core.environment.ui.text.EnvAnswer;
import org.safehaus.subutai.core.environment.ui.window.EnvironmentBuildProcessDetails;

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


@SuppressWarnings( "serial" )
public class EnvironmentsBuildProcessForm implements BuildProcessExecutionListener
{

    private static final String OK_ICON_SOURCE = "img/ok.png";
    private static final String ERROR_ICON_SOURCE = "img/cancel.png";
    private static final String LOAD_ICON_SOURCE = "img/spinner.gif";
    private static final String STATUS = "Status";
    private static final String ACTION = "Action";
    private Map<UUID, ExecutorService> executorServiceMap = new HashMap<>();
    private VerticalLayout contentRoot;
    private Table environmentsTable;
    private EnvironmentManagerPortalModule managerUI;
    private Button environmentsButton;

    public EnvironmentsBuildProcessForm( final EnvironmentManagerPortalModule managerUI )
    {
        this.managerUI = managerUI;

        contentRoot = new VerticalLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );

        environmentsTable = createTable( "Environments Build Process", 300 );

        environmentsButton = new Button( "View" );
        environmentsButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                updateTableData();
            }
        } );
        contentRoot.addComponent( environmentsButton );
        contentRoot.addComponent( environmentsTable );
    }


    private Table createTable( String caption, int size )
    {
        Table table = new Table( caption );
        table.addContainerProperty( "Name", String.class, null );
        table.addContainerProperty( STATUS, Embedded.class, null );
        table.addContainerProperty( "Info", Button.class, null );
        table.addContainerProperty( ACTION, Button.class, null );
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
        List<EnvironmentBuildProcess> processList =
                managerUI.getEnvironmentManager().getBuildProcesses();
        if ( !processList.isEmpty() )
        {
            for ( final EnvironmentBuildProcess process : processList )
            {
                Button viewButton = new Button( "Info" );
                viewButton.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent )
                    {
                        EnvironmentBuildProcessDetails detailsWindow =
                                new EnvironmentBuildProcessDetails( "Environment details" );
                        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                        String json = gson.toJson( process, EnvironmentBuildProcess.class );
                        detailsWindow.setContent( json );
                        contentRoot.getUI().addWindow( detailsWindow );
                        detailsWindow.setVisible( true );
                    }
                } );

                Button processButton = null;
                Button destroyButton = null;
                Embedded icon = null;

                switch ( process.getProcessStatusEnum() )
                {
                    case NEW_PROCESS:
                    {
                        processButton = new Button( "Build" );
                        icon = new Embedded( "", new ThemeResource( OK_ICON_SOURCE ) );
                        processButton.addClickListener( new Button.ClickListener()
                        {
                            @Override
                            public void buttonClick( final Button.ClickEvent clickEvent )
                            {
                                startBuildProcess( process );
                            }
                        } );

                        break;
                    }
                    case IN_PROGRESS:
                    {
                        processButton = new Button( "Terminate" );
                        icon = new Embedded( "", new ThemeResource( LOAD_ICON_SOURCE ) );
                        processButton.addClickListener( new Button.ClickListener()
                        {
                            @Override
                            public void buttonClick( final Button.ClickEvent clickEvent )
                            {
                                terminateBuildProcess( process );
                            }
                        } );
                        break;
                    }
                    case FAILED:
                        icon = new Embedded( "", new ThemeResource( ERROR_ICON_SOURCE ) );
                        break;
                    case TERMINATED:
                    {
                        icon = new Embedded( "", new ThemeResource( ERROR_ICON_SOURCE ) );
                        break;
                    }
                    case SUCCESSFUL:
                    {
                        icon = new Embedded( "", new ThemeResource( OK_ICON_SOURCE ) );
                        break;
                    }

                    default:
                    {
                        break;
                    }
                }

                destroyButton = new Button( "Destroy" );
                destroyButton.addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( final Button.ClickEvent clickEvent )
                    {
                        destroyBuildProcess( process );
                        environmentsButton.click();
                    }
                } );
                environmentsTable.addItem( new Object[] {
                        process.getEnvironmentName(), icon, viewButton, processButton,
                        destroyButton
                }, process.getUuid() );
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

        if ( executorServiceMap.containsKey( environmentBuildProcess.getUuid() ) )
        {
            ExecutorService executorService = executorServiceMap.get( environmentBuildProcess.getUuid() );
            if ( executorService.isTerminated() )
            {
                executorService.shutdown();
                executorServiceMap.remove( environmentBuildProcess.getUuid() );

                //TODO: need to use JPA to update entity properties instead of deleting and saving into C*
                managerUI.getEnvironmentManager().deleteBuildProcess( environmentBuildProcess );
                environmentBuildProcess.setCompleteStatus( true );
                environmentBuildProcess.setProcessStatusEnum( ProcessStatusEnum.TERMINATED );
                managerUI.getEnvironmentManager().saveBuildProcess( environmentBuildProcess );
            }
        }
        else
        {
            //TODO: check build process actual state in db and/or environments
        }
    }


    private void destroyBuildProcess( final EnvironmentBuildProcess environmentBuildProcess )
    {
        managerUI.getEnvironmentManager().deleteBuildProcess( environmentBuildProcess );
    }


    private void startBuildProcess( final EnvironmentBuildProcess environmentBuildProcess )
    {

        BuildProcessExecutor buildProcessExecutor = new BuildProcessExecutorImpl( environmentBuildProcess );
        buildProcessExecutor.addListener( this );
        ExecutorService executor = Executors.newCachedThreadPool();
        executorServiceMap.put( environmentBuildProcess.getUuid(), executor );


        buildProcessExecutor.execute( executor,
                new BuildCommandFactory( managerUI.getEnvironmentManager(), environmentBuildProcess ) );
        executor.shutdown();
    }


    @Override
    public void onExecutionEvent( final BuildProcessExecutionEvent event )
    {
        updateEnvironmentsTableStatus( event );
    }


    private void updateEnvironmentsTableStatus( final BuildProcessExecutionEvent event )
    {
        contentRoot.getUI().access( new Runnable()
        {
            @Override
            public void run()
            {
                Item row = environmentsTable.getItem( event.getEnvironmentBuildProcess().getUuid() );
                if ( row != null )
                {
                    Property p = row.getItemProperty( STATUS );
                    Button actionBtn = ( Button ) row.getItemProperty( ACTION ).getValue();
                    if ( BuildProcessExecutionEventType.START.equals( event.getEventType() ) )
                    {
                        actionBtn.setEnabled( false );
                        p.setValue( new Embedded( "", new ThemeResource( LOAD_ICON_SOURCE ) ) );
                        Notification.show( EnvAnswer.START.getAnswer() );

                        //TODO: need to use JPA to update entity properties instead of deleting and saving into C*
                        managerUI.getEnvironmentManager().deleteBuildProcess( event.getEnvironmentBuildProcess() );
                        EnvironmentBuildProcess ebp = event.getEnvironmentBuildProcess();
                        ebp.setCompleteStatus( true );
                        ebp.setProcessStatusEnum( ProcessStatusEnum.IN_PROGRESS );
                        managerUI.getEnvironmentManager().saveBuildProcess( ebp );
                    }
                    else if ( BuildProcessExecutionEventType.SUCCESS.equals( event.getEventType() ) )
                    {
                        p.setValue( new Embedded( "", new ThemeResource( OK_ICON_SOURCE ) ) );
                        Notification.show( EnvAnswer.SUCCESS.getAnswer() );

                        //TODO: need to use JPA to update entity properties instead of deleting and saving into C*
                        managerUI.getEnvironmentManager().deleteBuildProcess( event.getEnvironmentBuildProcess() );
                        EnvironmentBuildProcess ebp = event.getEnvironmentBuildProcess();
                        ebp.setCompleteStatus( true );
                        ebp.setProcessStatusEnum( ProcessStatusEnum.SUCCESSFUL );
                        managerUI.getEnvironmentManager().saveBuildProcess( ebp );
                    }
                    else if ( BuildProcessExecutionEventType.FAIL.equals( event.getEventType() ) )
                    {
                        p.setValue( new Embedded( "", new ThemeResource( ERROR_ICON_SOURCE ) ) );
                        Notification.show( EnvAnswer.FAIL.getAnswer() );

                        //TODO: need to use JPA to update entity properties instead of deleting and saving into C*
                        managerUI.getEnvironmentManager().deleteBuildProcess( event.getEnvironmentBuildProcess() );
                        EnvironmentBuildProcess ebp = event.getEnvironmentBuildProcess();
                        ebp.setCompleteStatus( true );
                        ebp.setProcessStatusEnum( ProcessStatusEnum.FAILED );
                        managerUI.getEnvironmentManager().saveBuildProcess( ebp );
                    }
                }
            }
        } );
    }


    public VerticalLayout getContentRoot()
    {
        return this.contentRoot;
    }
}
