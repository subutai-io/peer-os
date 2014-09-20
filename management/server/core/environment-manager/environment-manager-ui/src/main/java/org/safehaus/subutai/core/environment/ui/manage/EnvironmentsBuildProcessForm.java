package org.safehaus.subutai.core.environment.ui.manage;


import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.safehaus.subutai.core.environment.api.helper.ContainerBuildMessage;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerUI;
import org.safehaus.subutai.core.environment.ui.window.EnvironmentBuildProcessDetails;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings("serial")
public class EnvironmentsBuildProcessForm
{

    private final static Logger LOG = Logger.getLogger( EnvironmentsBuildProcessForm.class.getName() );
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

        Button saveBuildProcessButton = new Button( "Add" );
        saveBuildProcessButton.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {

                EnvironmentBuildProcess environmentBuildProcess = new EnvironmentBuildProcess();
                ContainerBuildMessage message = new ContainerBuildMessage();
                message.setNumberOfContainers( 2 );
                message.setTemplateName( "master" );
                message.setStrategy( "ROUND_ROBIN" );
                message.setEnvironmentUuid( UUID.randomUUID() );
                //                message.setTargetPeerId( managerUI.getPeerManager().getSiteId() );
                message.setTargetPeerId( UUID.fromString( "66ccf4d9-8ab4-3286-bb9a-8fe14bd19ea5" ) );
                environmentBuildProcess.addBuildBlock( message );
                ContainerBuildMessage message2 = new ContainerBuildMessage();
                message2.setNumberOfContainers( 2 );
                message2.setTemplateName( "master" );
                message2.setStrategy( "ROUND_ROBIN" );
                message2.setEnvironmentUuid( UUID.randomUUID() );
                //                message.setTargetPeerId( managerUI.getPeerManager().getSiteId() );
                message2.setTargetPeerId( UUID.fromString( "7e363225-2c4b-3ce3-8b33-d026d3367771" ) );
                environmentBuildProcess.addBuildBlock( message2 );
                managerUI.getEnvironmentManager().saveBuildProcess( environmentBuildProcess );
            }
        } );

        contentRoot.addComponent( saveBuildProcessButton );
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
                            // TODO create build task
                            try
                            {
                                managerUI.getEnvironmentManager().buildEnvironment( environmentBuildProcess );
                            }
                            catch ( NullPointerException e )
                            {
                                LOG.severe( e.getMessage() );
                            }
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
            }


            final Object rowId = environmentsTable.addItem( new Object[] {
                    environmentBuildProcess.getUuid(), progressIcon, viewEnvironmentInfoButton, processButton,
                    destroyButton
            }, null );
        }
        environmentsTable.refreshRowCache();
    }


    public VerticalLayout getContentRoot()
    {
        return this.contentRoot;
    }
}
