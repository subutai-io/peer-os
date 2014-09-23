package org.safehaus.subutai.core.environment.ui.manage;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerUI;
import org.safehaus.subutai.core.environment.ui.executor.BuildProcessExecutionListener;
import org.safehaus.subutai.core.environment.ui.window.EnvironmentBuildProcessDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings( "serial" )
public class EnvironmentsBuildProcessForm implements BuildProcessExecutionListener
{

    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentsBuildProcessForm.class.getName() );
    private VerticalLayout contentRoot;
    private Table environmentsTable;
    private EnvironmentManagerUI managerUI;


    public EnvironmentsBuildProcessForm( final EnvironmentManagerUI managerUI )
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
                Embedded progressIcon = null;
                Button destroyButton = null;

                switch ( environmentBuildProcess.getProcessStatusEnum() )
                {
                    case NEW_PROCESS:
                    {
                        processButton = new Button( "Build" );
                        progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
                        progressIcon.setVisible( false );

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
                                managerUI.getEnvironmentManager().deleteBuildProcess( environmentBuildProcess );
                            }
                        } );


                        break;
                    }
                    case IN_PROGRESS:
                    {
                        processButton = new Button( "Terminate" );
                        progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
                        progressIcon.setVisible( true );
                        processButton.addClickListener( new Button.ClickListener()
                        {
                            @Override
                            public void buttonClick( final Button.ClickEvent clickEvent )
                            {
                                // TODO create terminate task

                            }
                        } );
                        break;
                    }
                    case FAILED:
                    {
                        processButton = new Button( "Destroy" );
                        progressIcon = new Embedded( "", new ThemeResource( "img/cancel.png" ) );
                        progressIcon.setVisible( true );
                        processButton.addClickListener( new Button.ClickListener()
                        {
                            @Override
                            public void buttonClick( final Button.ClickEvent clickEvent )
                            {
                                // TODO create destroy task

                            }
                        } );
                        break;
                    }
                    case SUCCESSFUL:
                    {
                        processButton = new Button( "Configure" );
                        progressIcon = new Embedded( "", new ThemeResource( "img/ok.png" ) );
                        progressIcon.setVisible( true );
                        processButton.addClickListener( new Button.ClickListener()
                        {
                            @Override
                            public void buttonClick( final Button.ClickEvent clickEvent )
                            {
                                // TODO create configure logic

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
                        environmentBuildProcess.getUuid(), progressIcon, viewEnvironmentInfoButton, processButton,
                        destroyButton
                }, null );
            }
        }
        else
        {
            Notification.show( "No build process tasks", Notification.Type.HUMANIZED_MESSAGE );
        }
        environmentsTable.refreshRowCache();
    }


    private void startBuildProcess( final EnvironmentBuildProcess environmentBuildProcess )
    {

        ExecutorService executorService = Executors.newFixedThreadPool( 1 );
        executorService.execute( new Runnable()
        {
            public void run()
            {
                System.out.println( "Asynchronous task" );
                managerUI.getEnvironmentManager().buildEnvironment( environmentBuildProcess );
            }
        } );

        executorService.shutdown();
    }


    public VerticalLayout getContentRoot()
    {
        return this.contentRoot;
    }
}
