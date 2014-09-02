/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.solr.ui.manager;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.ui.SolrUI;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CompleteEvent;

import com.google.common.collect.Sets;
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
    private final Table nodesTable;
    private SolrClusterConfig solrClusterConfig;


    public Manager() {

        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 10 );
        contentRoot.setColumns( 1 );

        //tables go here
        nodesTable = createTableTemplate( "Nodes" );
        //tables go here

        HorizontalLayout controlsContent = new HorizontalLayout();
        controlsContent.setSpacing( true );

        Label clusterNameLabel = new Label( "Select the installation" );
        controlsContent.addComponent( clusterNameLabel );

        clusterCombo = new ComboBox();
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener() {
            @Override
            public void valueChange( Property.ValueChangeEvent event ) {
                solrClusterConfig = ( SolrClusterConfig ) event.getProperty().getValue();
                refreshUI();
            }
        } );
        controlsContent.addComponent( clusterCombo );

        Button refreshClustersBtn = new Button( "Refresh installations" );
        refreshClustersBtn.addStyleName( "default" );
        refreshClustersBtn.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                refreshClustersInfo();
            }
        } );
        controlsContent.addComponent( refreshClustersBtn );

        Button destroyClusterBtn = new Button( "Destroy Installation" );
        destroyClusterBtn.addStyleName( "default" );
        destroyClusterBtn.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                ConfirmationDialog alert = new ConfirmationDialog(
                        String.format( "Do you want to destroy the %s installation?",
                                solrClusterConfig.getClusterName() ), "Yes", "No" );
                alert.getOk().addClickListener( new Button.ClickListener() {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent ) {
                        UUID trackID = SolrUI.getSolrManager().uninstallCluster( solrClusterConfig.getClusterName() );
                        final ProgressWindow window =
                                new ProgressWindow( SolrUI.getExecutor(), SolrUI.getTracker(), trackID,
                                        SolrClusterConfig.PRODUCT_KEY );
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
        } );
        controlsContent.addComponent( destroyClusterBtn );


        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( nodesTable, 0, 1, 0, 9 );
    }


    public Component getContent() {
        return contentRoot;
    }


    private void show( String notification ) {
        Notification.show( notification );
    }


    private void populateTable( final Table table, Set<Agent> agents ) {

        table.removeAllItems();

        for ( final Agent agent : agents ) {
            final Button checkBtn = new Button( "Check" );
            checkBtn.addStyleName( "default" );
            final Button startBtn = new Button( "Start" );
            startBtn.addStyleName( "default" );
            final Button stopBtn = new Button( "Stop" );
            stopBtn.addStyleName( "default" );
            final Embedded progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
            stopBtn.setEnabled( false );
            startBtn.setEnabled( false );
            progressIcon.setVisible( false );

            table.addItem( new Object[] {
                    agent.getHostname(), checkBtn, startBtn, stopBtn, progressIcon
            }, null );

            checkBtn.addClickListener( new Button.ClickListener() {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent ) {
                    progressIcon.setVisible( true );
                    startBtn.setEnabled( false );
                    stopBtn.setEnabled( false );

                    SolrUI.getExecutor().execute(
                            new CheckTask( solrClusterConfig.getClusterName(), agent.getHostname(),
                                    new CompleteEvent() {

                                        public void onComplete( NodeState state ) {
                                            synchronized ( progressIcon ) {
                                                if ( state == NodeState.RUNNING ) {
                                                    stopBtn.setEnabled( true );
                                                }
                                                else if ( state == NodeState.STOPPED ) {
                                                    startBtn.setEnabled( true );
                                                }
                                                progressIcon.setVisible( false );
                                            }
                                        }
                                    } ) );
                }
            } );

            startBtn.addClickListener( new Button.ClickListener() {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent ) {
                    progressIcon.setVisible( true );
                    startBtn.setEnabled( false );
                    stopBtn.setEnabled( false );

                    SolrUI.getExecutor().execute(
                            new StartTask( solrClusterConfig.getClusterName(), agent.getHostname(),
                                    new CompleteEvent() {

                                        public void onComplete( NodeState state ) {
                                            synchronized ( progressIcon ) {
                                                if ( state == NodeState.RUNNING ) {
                                                    stopBtn.setEnabled( true );
                                                }
                                                else {
                                                    startBtn.setEnabled( true );
                                                }
                                                progressIcon.setVisible( false );
                                            }
                                        }
                                    } ) );
                }
            } );

            stopBtn.addClickListener( new Button.ClickListener() {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent ) {
                    progressIcon.setVisible( true );
                    startBtn.setEnabled( false );
                    stopBtn.setEnabled( false );

                    SolrUI.getExecutor().execute(
                            new StopTask( solrClusterConfig.getClusterName(), agent.getHostname(), new CompleteEvent() {

                                public void onComplete( NodeState state ) {
                                    synchronized ( progressIcon ) {
                                        if ( state == NodeState.STOPPED ) {
                                            startBtn.setEnabled( true );
                                        }
                                        else {
                                            stopBtn.setEnabled( true );
                                        }
                                        progressIcon.setVisible( false );
                                    }
                                }
                            } ) );
                }
            } );
        }
    }


    private void refreshUI() {
        if ( solrClusterConfig != null ) {
            populateTable( nodesTable, solrClusterConfig.getNodes() );
        }
        else {
            nodesTable.removeAllItems();
        }
    }


    public void refreshClustersInfo() {
        List<SolrClusterConfig> mongoClusterInfos = SolrUI.getSolrManager().getClusters();
        SolrClusterConfig clusterInfo = ( SolrClusterConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if ( mongoClusterInfos != null && mongoClusterInfos.size() > 0 ) {
            for ( SolrClusterConfig mongoClusterInfo : mongoClusterInfos ) {
                clusterCombo.addItem( mongoClusterInfo );
                clusterCombo.setItemCaption( mongoClusterInfo, mongoClusterInfo.getClusterName() );
            }
            if ( clusterInfo != null ) {
                for ( SolrClusterConfig mongoClusterInfo : mongoClusterInfos ) {
                    if ( mongoClusterInfo.getClusterName().equals( clusterInfo.getClusterName() ) ) {
                        clusterCombo.setValue( mongoClusterInfo );
                        return;
                    }
                }
            }
            else {
                clusterCombo.setValue( mongoClusterInfos.iterator().next() );
            }
        }
    }


    private Table createTableTemplate( String caption ) {
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
                    Agent lxcAgent = SolrUI.getAgentManager().getAgentByHostname( lxcHostname );
                    if ( lxcAgent != null ) {
                        TerminalWindow terminal = new TerminalWindow( Sets.newHashSet( lxcAgent ), SolrUI.getExecutor(),
                                SolrUI.getCommandRunner(), SolrUI.getAgentManager() );
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
}
