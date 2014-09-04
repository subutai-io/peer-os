/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.oozie.manager;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.api.oozie.OozieConfig;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;
import org.safehaus.subutai.ui.oozie.OozieUI;

import com.google.common.collect.Sets;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;


/**
 * @author dilshat
 */
public class Manager {

    private final GridLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table serverTable;
    private final Table clientsTable;
    private OozieConfig config;


    public Manager() {

        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 11 );
        contentRoot.setColumns( 1 );

        //tables go here
        serverTable = createServerTableTemplate( "Server" );
        clientsTable = createClientsTableTemplate( "Clients" );
        //tables go here

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );

        clusterCombo = new ComboBox();
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener() {
            @Override
            public void valueChange( Property.ValueChangeEvent event ) {
                config = ( OozieConfig ) event.getProperty().getValue();
                refreshUI();
            }
        } );

        controlsContent.addComponent( clusterCombo );

        Button refreshClustersBtn = new Button( "Refresh clusters" );
        refreshClustersBtn.addStyleName( "default" );
        refreshClustersBtn.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                refreshClustersInfo();
            }
        } );

        controlsContent.addComponent( refreshClustersBtn );

        Button checkAllBtn = new Button( "Check all" );
        checkAllBtn.addStyleName( "default" );
        checkAllBtn.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                checkNodesStatus( serverTable );
            }
        } );

        // TODO add restart hadoop button

        Button destroyClusterBtn = new Button( "Destroy cluster" );
        destroyClusterBtn.addStyleName( "default" );
        destroyClusterBtn.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                if ( config != null ) {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to destroy the %s cluster?", config.getClusterName() ), "Yes",
                            "No" );
                    alert.getOk().addClickListener( new Button.ClickListener() {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent ) {
                            UUID trackID = OozieUI.getOozieManager().uninstallCluster( config.getClusterName() );
                            ProgressWindow window =
                                    new ProgressWindow( OozieUI.getExecutor(), OozieUI.getTracker(), trackID,
                                            OozieConfig.PRODUCT_KEY );
                            window.getWindow().addCloseListener( new Window.CloseListener() {
                                @Override
                                public void windowClose( Window.CloseEvent closeEvent ) {
                                    refreshClustersInfo();
                                }
                            } );
                            contentRoot.getUI().addWindow( window.getWindow() );
                        }
                    } );

                    contentRoot.getUI().addWindow( alert.getAlert() );
                }
                else {
                    show( "Please, select cluster" );
                }
            }
        } );

        controlsContent.addComponent( destroyClusterBtn );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( serverTable, 0, 1, 0, 5 );
        contentRoot.addComponent( clientsTable, 0, 6, 0, 10 );
    }


    public static void checkNodesStatus( Table table ) {
        for ( Object o : table.getItemIds() ) {
            int rowId = ( Integer ) o;
            Item row = table.getItem( rowId );
            Button checkBtn = ( Button ) ( row.getItemProperty( "Check" ).getValue() );
            checkBtn.click();
        }
    }


    private void refreshUI() {
        if ( config != null ) {
            populateServerTable( serverTable, config.getServer() );
            populateClientsTable( clientsTable, config.getClients() );
        }
        else {
            serverTable.removeAllItems();
            clientsTable.removeAllItems();
        }
    }


    private void populateServerTable( final Table table, final String agentHostname ) {

        table.removeAllItems();
        final Button checkBtn = new Button( "Check" );
        checkBtn.addStyleName( "default" );
        final Button startBtn = new Button( "Start" );
        startBtn.addStyleName( "default" );
        final Button stopBtn = new Button( "Stop" );
        stopBtn.addStyleName( "default" );
        final Embedded progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
        progressIcon.setVisible( false );

        final Object rowId = table.addItem( new Object[] {
                        agentHostname, checkBtn, startBtn, stopBtn, progressIcon
                }, null );

        checkBtn.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                progressIcon.setVisible( true );

                UUID trackID = OozieUI.getOozieManager().checkServerStatus( config );
                ProgressWindow window = new ProgressWindow( OozieUI.getExecutor(), OozieUI.getTracker(), trackID,
                        OozieConfig.PRODUCT_KEY );
                window.getWindow().addCloseListener( new Window.CloseListener() {
                    @Override
                    public void windowClose( Window.CloseEvent closeEvent ) {
                        refreshClustersInfo();
                    }
                } );
                contentRoot.getUI().addWindow( window.getWindow() );
            }
        } );

        startBtn.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                progressIcon.setVisible( true );
                startBtn.setEnabled( false );
                stopBtn.setEnabled( false );

                UUID trackID = OozieUI.getOozieManager().startServer( config );
                ProgressWindow window = new ProgressWindow( OozieUI.getExecutor(), OozieUI.getTracker(), trackID,
                        OozieConfig.PRODUCT_KEY );
                window.getWindow().addCloseListener( new Window.CloseListener() {
                    @Override
                    public void windowClose( Window.CloseEvent closeEvent ) {
                        refreshClustersInfo();
                    }
                } );
                contentRoot.getUI().addWindow( window.getWindow() );
            }
        } );

        stopBtn.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                progressIcon.setVisible( true );

                UUID trackID = OozieUI.getOozieManager().stopServer( config );
                ProgressWindow window = new ProgressWindow( OozieUI.getExecutor(), OozieUI.getTracker(), trackID,
                        OozieConfig.PRODUCT_KEY );
                window.getWindow().addCloseListener( new Window.CloseListener() {
                    @Override
                    public void windowClose( Window.CloseEvent closeEvent ) {
                        refreshClustersInfo();
                    }
                } );
                contentRoot.getUI().addWindow( window.getWindow() );
            }
        } );
    }


    public void refreshClustersInfo() {
        List<OozieConfig> info = OozieUI.getOozieManager().getClusters();
        OozieConfig clusterInfo = ( OozieConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if ( info != null && info.size() > 0 ) {
            for ( OozieConfig oozieConfig : info ) {
                clusterCombo.addItem( oozieConfig );
                clusterCombo.setItemCaption( oozieConfig, oozieConfig.getClusterName() );
            }
            if ( clusterInfo != null ) {
                for ( OozieConfig oozieInfo : info ) {
                    if ( oozieInfo.getClusterName().equals( clusterInfo.getClusterName() ) ) {
                        clusterCombo.setValue( oozieInfo );
                        return;
                    }
                }
            }
            else {
                clusterCombo.setValue( info.iterator().next() );
            }
        }
    }


    private void populateClientsTable( final Table table, Set<String> clientHostnames ) {

        table.removeAllItems();

        for ( final String agent : clientHostnames ) {
            final Embedded progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
            progressIcon.setVisible( false );

            final Object rowId = table.addItem( new Object[] {
                            agent,
                    }, null );
        }
    }


    private Table createServerTableTemplate( String caption ) {
        final Table table = new Table( caption );
        table.addContainerProperty( "Host", String.class, null );
        table.addContainerProperty( "Check", Button.class, null );
        table.addContainerProperty( "Start", Button.class, null );
        table.addContainerProperty( "Stop", Button.class, null );
        table.addContainerProperty( "Status", Embedded.class, null );
        table.setSizeFull();
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );

        table.addItemClickListener( new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick( ItemClickEvent event ) {
                if ( event.isDoubleClick() ) {
                    String lxcHostname =
                            ( String ) table.getItem( event.getItemId() ).getItemProperty( "Host" ).getValue();
                    Agent lxcAgent = OozieUI.getAgentManager().getAgentByHostname( lxcHostname );
                    if ( lxcAgent != null ) {
                        TerminalWindow terminal =
                                new TerminalWindow( Sets.newHashSet( lxcAgent ), OozieUI.getExecutor(),
                                        OozieUI.getCommandRunner(), OozieUI.getAgentManager() );
                        contentRoot.getUI().addWindow( terminal.getWindow() );
                    }
                    else {
                        show( "Agent is not connected" );
                    }
                }
            }
        } );
        return table;
    }


    private void show( String notification ) {
        Notification.show( notification );
    }


    private Table createClientsTableTemplate( String caption ) {
        final Table table = new Table( caption );
        table.addContainerProperty( "Host", String.class, null );
        table.setSizeFull();
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );

        table.addItemClickListener( new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick( ItemClickEvent event ) {
                if ( event.isDoubleClick() ) {
                    String lxcHostname =
                            ( String ) table.getItem( event.getItemId() ).getItemProperty( "Host" ).getValue();
                    Agent lxcAgent = OozieUI.getAgentManager().getAgentByHostname( lxcHostname );
                    if ( lxcAgent != null ) {
                        TerminalWindow terminal =
                                new TerminalWindow( Sets.newHashSet( lxcAgent ), OozieUI.getExecutor(),
                                        OozieUI.getCommandRunner(), OozieUI.getAgentManager() );
                        contentRoot.getUI().addWindow( terminal.getWindow() );
                    }
                    else {
                        show( "Agent is not connected" );
                    }
                }
            }
        } );
        return table;
    }


    public Component getContent() {
        return contentRoot;
    }
}
