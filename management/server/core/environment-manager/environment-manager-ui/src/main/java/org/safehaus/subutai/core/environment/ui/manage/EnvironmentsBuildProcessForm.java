package org.safehaus.subutai.core.environment.ui.manage;


import java.util.List;
import java.util.UUID;

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
public class EnvironmentsBuildProcessForm {

    private VerticalLayout contentRoot;
    private Table environmentsTable;
    private EnvironmentManagerUI managerUI;


    public EnvironmentsBuildProcessForm( final EnvironmentManagerUI managerUI ) {
        this.managerUI = managerUI;

        contentRoot = new VerticalLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );

        environmentsTable = createTable( "Environments Build Process", 300 );

        Button getEnvironmentsButton = new Button( "View" );

        getEnvironmentsButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent ) {
                updateTableData();
            }
        } );

        contentRoot.addComponent( getEnvironmentsButton );

        Button saveBuildProcessButton = new Button( "Add" );

        saveBuildProcessButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent ) {

                EnvironmentBuildProcess environmentBuildProcess = new EnvironmentBuildProcess();
                ContainerBuildMessage message = new ContainerBuildMessage();
                message.setNumberOfContainers( 4 );
                message.setTemplateName( "master" );
                message.setStrategy( "ROUND_ROBIN" );
                message.setEnvironmentUuid( UUID.randomUUID().toString() );

                environmentBuildProcess.addBuildBlock( new ContainerBuildMessage() );

                managerUI.getEnvironmentManager().saveBuildProcess( environmentBuildProcess );
            }
        } );

        contentRoot.addComponent( saveBuildProcessButton );

        contentRoot.addComponent( environmentsTable );
    }


    private Table createTable( String caption, int size ) {
        Table table = new Table( caption );
        table.addContainerProperty( "Name", UUID.class, null );
        table.addContainerProperty( "Date", String.class, null );
        table.addContainerProperty( "Status", Embedded.class, null );
        table.addContainerProperty( "Info", Button.class, null );
        table.addContainerProperty( "Destroy", Button.class, null );
        //        table.setWidth( 100, Sizeable.UNITS_PERCENTAGE );
        //        table.setHeight( size, Sizeable.UNITS_PIXELS );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setEnabled( true );
        table.setImmediate( true );
        table.setSizeFull();
        //        table.addListener( new ItemClickEvent.ItemClickListener() {
        //
        //            public void itemClick( ItemClickEvent event ) {
        //                if ( event.isDoubleClick() ) {
        //
        //                }
        //            }
        //        } );
        return table;
    }


    private void updateTableData() {
        environmentsTable.removeAllItems();
        List<EnvironmentBuildProcess> environmentBuildProcessList =
                managerUI.getEnvironmentManager().getBuildProcesses();
        for ( final EnvironmentBuildProcess environmentBuildProcess : environmentBuildProcessList )
        {
            Button viewEnvironmentInfoButton = new Button( "Info" );
            viewEnvironmentInfoButton.addClickListener( new Button.ClickListener() {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent ) {
                    EnvironmentBuildProcessDetails detailsWindow =
                            new EnvironmentBuildProcessDetails( "Environment details" );
                    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                    String json = gson.toJson( environmentBuildProcess, EnvironmentBuildProcess.class );
                    detailsWindow.setContent( json );
                    contentRoot.getUI().addWindow( detailsWindow);
                    detailsWindow.setVisible( true );
                }
            } );

            Button processButton = null;
            Embedded progressIcon = null;

            switch ( environmentBuildProcess.getProcessStatusEnum() )
            {
                case NEW_PROCESS:
                {
                    processButton = new Button( "Build" );
                    progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
                    progressIcon.setVisible( true );
                    processButton.addClickListener( new Button.ClickListener() {
                        @Override
                        public void buttonClick( final Button.ClickEvent clickEvent ) {
                            // TODO create build thred
                            //                            managerUI.getPeerManager().createContainers();
                        }
                    } );
                    break;
                }
                case IN_PROGRESS:
                {
                    processButton = new Button( "Terminate" );
                    progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
                    progressIcon.setVisible( true );
                    processButton.addClickListener( new Button.ClickListener() {
                        @Override
                        public void buttonClick( final Button.ClickEvent clickEvent ) {
                            // TODO create build thred

                        }
                    } );
                    break;
                }
                case FAILED:
                {
                    processButton = new Button( "Destroy" );
                    progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
                    progressIcon.setVisible( true );
                    processButton.addClickListener( new Button.ClickListener() {
                        @Override
                        public void buttonClick( final Button.ClickEvent clickEvent ) {
                            // TODO create build thred

                        }
                    } );
                    break;
                }
                case SUCCESSFUL:
                {
                    processButton = new Button( "Configure" );
                    progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
                    progressIcon.setVisible( true );
                    processButton.addClickListener( new Button.ClickListener() {
                        @Override
                        public void buttonClick( final Button.ClickEvent clickEvent ) {
                            // TODO create build thred

                        }
                    } );
                    break;
                }
            }


            final Object rowId = environmentsTable.addItem( new Object[] {
                    environmentBuildProcess.getUuid(), "$date", progressIcon, viewEnvironmentInfoButton, processButton
            }, null );
        }
        environmentsTable.refreshRowCache();
    }


    public VerticalLayout getContentRoot() {
        return this.contentRoot;
    }
}
