package org.safehaus.subutai.core.environment.ui.manage;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.core.environment.api.helper.ContainerBuildMessage;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.ui.EnvironmentManagerUI;
import org.safehaus.subutai.core.environment.ui.window.EnvironmentBuildProcessDetails;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings("serial")
public class EnvironmentsBuildProcessForm {

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

        getEnvironmentsButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {
                updateTableData();
            }
        } );

        contentRoot.addComponent( getEnvironmentsButton );

        Button saveBuildProcessButton = new Button( "Add" );

        saveBuildProcessButton.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {

                EnvironmentBuildProcess environmentBuildProcess = new EnvironmentBuildProcess();
                environmentBuildProcess.addBuildBlock( new ContainerBuildMessage() );

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


    private void updateTableData()
    {
        environmentsTable.removeAllItems();
        List<EnvironmentBuildProcess> environmentList = managerUI.getEnvironmentManager().getBuildProcesses();
        for ( final EnvironmentBuildProcess environmentBuildProcess : environmentList )
        {
            Button viewEnvironmentInfoButton = new Button( "Info" );
            viewEnvironmentInfoButton.addClickListener( new Button.ClickListener() {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent )
                {
                    EnvironmentBuildProcessDetails detailsWindow =
                            new EnvironmentBuildProcessDetails( "Environment details" );
                    detailsWindow.setContent( environmentBuildProcess );
                    contentRoot.getUI().addWindow( detailsWindow );
                    detailsWindow.setVisible( true );
                }
            } );

            Button terminateButton = new Button( "Terminate" );
            terminateButton.addClickListener( new Button.ClickListener() {
                @Override
                public void buttonClick( final Button.ClickEvent clickEvent )
                {
                    // TODO terminate environment build process
                    Notification.show( "Terminated", Notification.Type.WARNING_MESSAGE );
                }
            } );

            final Embedded progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
            progressIcon.setVisible( true );

            final Object rowId = environmentsTable.addItem( new Object[] {
                    environmentBuildProcess.getUuid(), "$date", progressIcon, viewEnvironmentInfoButton, terminateButton
            }, null );
        }
        environmentsTable.refreshRowCache();
    }


    public VerticalLayout getContentRoot()
    {
        return this.contentRoot;
    }
}
