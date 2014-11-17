package org.safehaus.subutai.core.environment.ui.manage;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.helper.ProcessStatusEnum;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerPortalModule;
import org.safehaus.subutai.core.environment.ui.executor.build.BuildCommandFactory;
import org.safehaus.subutai.core.environment.ui.executor.build.BuildProcessExecutionEvent;
import org.safehaus.subutai.core.environment.ui.executor.build.BuildProcessExecutionEventType;
import org.safehaus.subutai.core.environment.ui.executor.build.BuildProcessExecutionListener;
import org.safehaus.subutai.core.environment.ui.executor.build.BuildProcessExecutor;
import org.safehaus.subutai.core.environment.ui.executor.build.BuildProcessExecutorImpl;
import org.safehaus.subutai.core.environment.ui.text.EnvAnswer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


@SuppressWarnings( "serial" )
public class EnvironmentsBuildProcessForm implements BuildProcessExecutionListener
{

    private static final String OK_ICON_SOURCE = "img/ok.png";
    private static final String ERROR_ICON_SOURCE = "img/cancel.png";
    private static final String LOAD_ICON_SOURCE = "img/spinner.gif";
    private static final String STATUS = "Status";
    private static final String ACTION = "Action";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private Map<UUID, ExecutorService> executorServiceMap = new HashMap<>();
    private VerticalLayout contentRoot;
    private Table environmentsTable;
    private EnvironmentManagerPortalModule module;
    private Button environmentsButton;


    public EnvironmentsBuildProcessForm( final EnvironmentManagerPortalModule module )
    {
        this.module = module;

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
        List<EnvironmentBuildProcess> processList = module.getEnvironmentManager().getBuildProcesses();
        if ( !processList.isEmpty() )
        {
            for ( final EnvironmentBuildProcess process : processList )
            {
                addProcessToTable( process );
            }
        }
        else
        {
            Notification.show( EnvAnswer.NO_BUILD_PROCESS.getAnswer() );
        }
        environmentsTable.refreshRowCache();
    }


    public void addProcessToTable( final EnvironmentBuildProcess process )
    {
        Button viewButton = new Button( "Info" );
        viewButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                Window window = genProcessWindow( process );
                window.setVisible( true );
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
        try
        {
            EnvironmentBlueprint blueprint =
                    module.getEnvironmentManager().getEnvironmentBlueprint( process.getBlueprintId() );
            environmentsTable.addItem( new Object[] {
                    blueprint.getName(), icon, viewButton, processButton, destroyButton
            }, process.getId() );
        }
        catch ( EnvironmentManagerException e )
        {
            Notification.show( e.getMessage() );
        }
    }


    private Window genProcessWindow( final EnvironmentBuildProcess process )
    {
        Window window = createWindow( "Environment details" );
        TextArea area = new TextArea();
        area.setSizeFull();
        area.setValue( GSON.toJson( process ) );
        window.setContent( area );
        contentRoot.getUI().addWindow( window );
        return window;
    }


    private Window createWindow( String caption )
    {
        Window window = new Window();
        window.setCaption( caption );
        window.setWidth( "800px" );
        window.setHeight( "500px" );
        window.setModal( true );
        window.setClosable( true );
        return window;
    }


    private void terminateBuildProcess( final EnvironmentBuildProcess environmentBuildProcess )
    {

        UUID uuid = UUID.fromString( environmentBuildProcess.getId().toString() );
        if ( executorServiceMap.containsKey( uuid ) )
        {
            ExecutorService executorService = executorServiceMap.get( uuid );
            if ( !executorService.isTerminated() )
            {
                executorService.shutdown();
                executorServiceMap.remove( uuid );
                environmentBuildProcess.setProcessStatusEnum( ProcessStatusEnum.TERMINATED );
                try
                {
                    module.getEnvironmentManager().saveBuildProcess( environmentBuildProcess );
                    Notification.show( "Saved" );
                }
                catch ( EnvironmentManagerException e )
                {
                    Notification.show( e.toString() );
                }
            }
            Notification.show( "Terminated" );
        }
        else
        {
            Notification.show( "No such process" );
            //TODO: check build process actual state in db and/or environments
        }
    }


    private void destroyBuildProcess( final EnvironmentBuildProcess environmentBuildProcess )
    {
        module.getEnvironmentManager().deleteBuildProcess( environmentBuildProcess );
    }


    public void startBuildProcess( final EnvironmentBuildProcess environmentBuildProcess )
    {

        BuildProcessExecutor buildProcessExecutor = new BuildProcessExecutorImpl( environmentBuildProcess );
        buildProcessExecutor.addListener( this );
        ExecutorService executor = Executors.newCachedThreadPool();


        buildProcessExecutor.execute( executor,
                new BuildCommandFactory( module.getEnvironmentManager(), environmentBuildProcess ) );
        executor.shutdown();
    }


    private void configureEnvironment( final EnvironmentBuildProcess environmentBuildProcess )
    {
        //TODO: configure code
    }


    @Override
    public void onExecutionEvent( final BuildProcessExecutionEvent event )
    {
        updateEnvironmentsTableStatus( event );
    }


    private void updateEnvironmentsTableStatus( final BuildProcessExecutionEvent event )
    {
        Notification.show( event.getExceptionMessage() );
        contentRoot.getUI().access( new Runnable()
        {
            @Override
            public void run()
            {
                Item row = environmentsTable.getItem( event.getEnvironmentBuildProcess().getId() );
                EnvironmentBuildProcess ebp = event.getEnvironmentBuildProcess();
                if ( row != null )
                {
                    Property p = row.getItemProperty( STATUS );
                    Button actionBtn = ( Button ) row.getItemProperty( ACTION ).getValue();
                    if ( BuildProcessExecutionEventType.START.equals( event.getEventType() ) )
                    {
                        actionBtn.setEnabled( false );
                        p.setValue( new Embedded( "", new ThemeResource( LOAD_ICON_SOURCE ) ) );

                        //                        ebp.setProcessStatusEnum( ProcessStatusEnum.IN_PROGRESS );
                        //                        module.getEnvironmentManager().saveBuildProcess( ebp );
                    }
                    else if ( BuildProcessExecutionEventType.SUCCESS.equals( event.getEventType() ) )
                    {
                        p.setValue( new Embedded( "", new ThemeResource( OK_ICON_SOURCE ) ) );

                        //                        ebp.setProcessStatusEnum( ProcessStatusEnum.SUCCESSFUL );
                        //                        module.getEnvironmentManager().saveBuildProcess( ebp );
                    }
                    else if ( BuildProcessExecutionEventType.FAIL.equals( event.getEventType() ) )
                    {
                        p.setValue( new Embedded( "", new ThemeResource( ERROR_ICON_SOURCE ) ) );

                        //                        ebp.setProcessStatusEnum( ProcessStatusEnum.FAILED );
                    }
                }

                //                module.getEnvironmentManager().saveBuildProcess( ebp );
            }
        } );
    }


    public VerticalLayout getContentRoot()
    {
        return this.contentRoot;
    }
}
