/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.shark.ui.manager;


import com.google.common.collect.Sets;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import org.safehaus.subutai.plugin.shark.api.Config;
import org.safehaus.subutai.plugin.shark.ui.SharkUI;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * @author dilshat
 */
public class Manager
{

    private final GridLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table nodesTable;
    private Config config;


    public Manager()
    {

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

        Label clusterNameLabel = new Label( "Select the cluster" );
        controlsContent.addComponent( clusterNameLabel );

        clusterCombo = new ComboBox();
        clusterCombo.setImmediate( true );
        clusterCombo.setTextInputAllowed( false );
        clusterCombo.setWidth( 200, Sizeable.Unit.PIXELS );
        clusterCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( Property.ValueChangeEvent event )
            {
                config = ( Config ) event.getProperty().getValue();
                refreshUI();
            }
        } );

        controlsContent.addComponent( clusterCombo );

        Button refreshClustersBtn = new Button( "Refresh clusters" );
        refreshClustersBtn.addStyleName( "default" );
        refreshClustersBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                refreshClustersInfo();
            }
        } );

        controlsContent.addComponent( refreshClustersBtn );

        Button destroyClusterBtn = new Button( "Destroy cluster" );
        destroyClusterBtn.addStyleName( "default" );
        destroyClusterBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config != null )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                        String.format( "Do you want to destroy the %s cluster?", config.getClusterName() ),
                        "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID trackID = SharkUI.getSharkManager().uninstallCluster( config.getClusterName() );
                            ProgressWindow window = new ProgressWindow( SharkUI.getExecutor(), SharkUI.getTracker(),
                                trackID, Config.PRODUCT_KEY );
                            window.getWindow().addCloseListener( new Window.CloseListener()
                            {
                                @Override
                                public void windowClose( Window.CloseEvent closeEvent )
                                {
                                    refreshClustersInfo();
                                }
                            } );
                            contentRoot.getUI().addWindow( window.getWindow() );
                        }
                    } );

                    contentRoot.getUI().addWindow( alert.getAlert() );
                }
                else
                {
                    show( "Please, select cluster" );
                }
            }
        } );

        controlsContent.addComponent( destroyClusterBtn );

        Button addNodeBtn = new Button( "Add Node" );
        addNodeBtn.addStyleName( "default" );
        addNodeBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config != null )
                {
                    SparkClusterConfig info = SharkUI.getSparkManager().
                        getCluster( config.getClusterName() );
                    if ( info != null )
                    {
                        Set<Agent> nodes = new HashSet<>( info.getAllNodes() );
                        nodes.removeAll( config.getNodes() );
                        if ( !nodes.isEmpty() )
                        {
                            AddNodeWindow addNodeWindow = new AddNodeWindow( config, nodes );
                            contentRoot.getUI().addWindow( addNodeWindow );
                            addNodeWindow.addCloseListener( new Window.CloseListener()
                            {
                                @Override
                                public void windowClose( Window.CloseEvent closeEvent )
                                {
                                    refreshClustersInfo();
                                }
                            } );
                        }
                        else
                        {
                            show( "All nodes in corresponding Spark cluster have Shark installed" );
                        }
                    }
                    else
                    {
                        show( "Spark cluster info not found" );
                    }
                }
                else
                {
                    show( "Please, select cluster" );
                }
            }
        } );

        controlsContent.addComponent( addNodeBtn );

        Button actualizeBtn = new Button( "Update Master" );
        actualizeBtn.addStyleName( "default" );
        actualizeBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config != null )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                        String.format( "Do you want to Actualize master IP in %s cluster?", config.getClusterName() ),
                        "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID trackID = SharkUI.getSharkManager().actualizeMasterIP( config.getClusterName() );
                            ProgressWindow window = new ProgressWindow( SharkUI.getExecutor(), SharkUI.getTracker(),
                                trackID, Config.PRODUCT_KEY );
                            window.getWindow().addCloseListener( new Window.CloseListener()
                            {
                                @Override
                                public void windowClose( Window.CloseEvent closeEvent )
                                {
                                    refreshClustersInfo();
                                }
                            } );
                            contentRoot.getUI().addWindow( window.getWindow() );
                        }
                    } );
                }
                else
                {
                    show( "Please, select cluster" );
                }
            }
        } );

        controlsContent.addComponent( actualizeBtn );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( nodesTable, 0, 1, 0, 9 );

    }


    private Table createTableTemplate( String caption )
    {
        final Table table = new Table( caption );
        table.addContainerProperty( "Host", String.class, null );
        table.addContainerProperty( "Destroy", Button.class, null );
        table.setSizeFull();
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );

        table.addItemClickListener( new ItemClickEvent.ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                if ( event.isDoubleClick() )
                {
                    String lxcHostname = ( String ) table.getItem( event.getItemId() ).getItemProperty( "Host" )
                        .getValue();
                    Agent lxcAgent = SharkUI.getAgentManager().getAgentByHostname( lxcHostname );
                    if ( lxcAgent != null )
                    {
                        TerminalWindow terminal = new TerminalWindow( Sets.newHashSet( lxcAgent ),
                            SharkUI.getExecutor(), SharkUI.getCommandRunner(), SharkUI.getAgentManager() );
                        contentRoot.getUI().addWindow( terminal.getWindow() );
                    }
                    else
                    {
                        show( "Agent is not connected" );
                    }
                }
            }
        } );
        return table;
    }


    private void refreshUI()
    {
        if ( config != null )
        {
            populateTable( nodesTable, config.getNodes() );
        }
        else
        {
            nodesTable.removeAllItems();
        }
    }


    public void refreshClustersInfo()
    {
        List<Config> clustersInfo = SharkUI.getSharkManager().getClusters();

        Config clusterInfo = ( Config ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if ( clustersInfo != null && clustersInfo.size() > 0 )
        {
            for ( Config mongoClusterInfo : clustersInfo )
            {
                clusterCombo.addItem( mongoClusterInfo );
                clusterCombo.setItemCaption( mongoClusterInfo,
                    mongoClusterInfo.getClusterName() );
            }
            if ( clusterInfo != null )
            {
                for ( Config mongoClusterInfo : clustersInfo )
                {
                    if ( mongoClusterInfo.getClusterName().equals( clusterInfo.getClusterName() ) )
                    {
                        clusterCombo.setValue( mongoClusterInfo );
                        return;
                    }
                }
            }
            else
            {
                clusterCombo.setValue( clustersInfo.iterator().next() );
            }
        }
    }


    private void show( String notification )
    {
        Notification.show( notification );
    }


    private void populateTable( final Table table, Set<Agent> agents )
    {

        table.removeAllItems();

        for ( final Agent agent : agents )
        {
            final Button destroyBtn = new Button( "Destroy" );
            destroyBtn.addStyleName( "default" );

            table.addItem( new Object[] {
                    agent.getHostname() + String.format( " [%s]", agent.getListIP().get( 0 ) ),
                    destroyBtn
                },
                null
            );

            destroyBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                        String.format( "Do you want to destroy the %s node?", agent.getHostname() ),
                        "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID trackID = SharkUI.getSharkManager()
                                .destroyNode( config.getClusterName(), agent.getHostname() );
                            ProgressWindow window = new ProgressWindow( SharkUI.getExecutor(), SharkUI.getTracker(),
                                trackID, Config.PRODUCT_KEY );
                            window.getWindow().addCloseListener( new Window.CloseListener()
                            {
                                @Override
                                public void windowClose( Window.CloseEvent closeEvent )
                                {
                                    refreshClustersInfo();
                                }
                            } );
                            contentRoot.getUI().addWindow( window.getWindow() );
                        }
                    } );

                    contentRoot.getUI().addWindow( alert.getAlert() );
                }
            } );
        }
    }


    public Component getContent()
    {
        return contentRoot;
    }

}
