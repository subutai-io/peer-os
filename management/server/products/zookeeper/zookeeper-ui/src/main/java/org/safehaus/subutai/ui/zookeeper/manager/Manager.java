/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.zookeeper.manager;


import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;

import org.safehaus.subutai.api.hadoop.HadoopClusterConfig;
import org.safehaus.subutai.api.zookeeper.Config;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.ui.zookeeper.ZookeeperUI;

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
        contentRoot.setRows( 11 );
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
            public void buttonClick( Button.ClickEvent event )
            {
                refreshClustersInfo();
            }
        } );
        controlsContent.addComponent( refreshClustersBtn );

        Button checkAllBtn = new Button( "Check all" );
        checkAllBtn.addStyleName( "default" );
        checkAllBtn.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                checkNodesStatus( nodesTable );
            }

        } );
        controlsContent.addComponent( checkAllBtn );

        Button startAllBtn = new Button( "Start all" );
        startAllBtn.addStyleName( "default" );
        startAllBtn.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                startAllNodes( nodesTable );
            }

        } );
        controlsContent.addComponent( startAllBtn );

        Button stopAllBtn = new Button( "Stop all" );
        stopAllBtn.addStyleName( "default" );
        stopAllBtn.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                stopAllNodes( nodesTable );
            }

        } );
        controlsContent.addComponent( stopAllBtn );

        Button destroyClusterBtn = new Button( "Destroy cluster" );
        destroyClusterBtn.addStyleName( "default" );
        destroyClusterBtn.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
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
                            UUID trackID = ZookeeperUI.getManager().uninstallCluster( config.getClusterName() );
                            ProgressWindow window = new ProgressWindow( ZookeeperUI.getExecutor(),
                                ZookeeperUI.getTracker(), trackID, Config.PRODUCT_KEY );
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
            public void buttonClick( Button.ClickEvent event )
            {
                if ( config != null )
                {
                    if ( config.isStandalone() )
                    {
                        ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to add node to the %s cluster?", config.getClusterName() ),
                            "Yes", "No" );
                        alert.getOk().addClickListener( new Button.ClickListener()
                        {
                            @Override
                            public void buttonClick( Button.ClickEvent clickEvent )
                            {
                                UUID trackID = ZookeeperUI.getManager().addNode( config.getClusterName() );
                                ProgressWindow window = new ProgressWindow( ZookeeperUI.getExecutor(),
                                    ZookeeperUI.getTracker(), trackID, Config.PRODUCT_KEY );
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
                        HadoopClusterConfig info = ZookeeperUI.getHadoopManager()
                            .getCluster( config.getClusterName() );

                        if ( info != null )
                        {
                            Set<Agent> nodes = new HashSet<Agent>( info.getAllNodes() );
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
                                show( "All nodes in corresponding Hadoop cluster have Zookeeper installed" );
                            }
                        }
                        else
                        {
                            show( "Hadoop cluster info not found" );
                        }
                    }
                }
                else
                {
                    show( "Please, select cluster" );
                }
            }
        } );
        controlsContent.addComponent( addNodeBtn );

        HorizontalLayout customPropertyContent = new HorizontalLayout();
        customPropertyContent.setSpacing( true );

        Label fileLabel = new Label( "File" );
        customPropertyContent.addComponent( fileLabel );
        final TextField fileTextField = new TextField();
        customPropertyContent.addComponent( fileTextField );
        Label propertyNameLabel = new Label( "Property Name" );
        customPropertyContent.addComponent( propertyNameLabel );
        final TextField propertyNameTextField = new TextField();
        customPropertyContent.addComponent( propertyNameTextField );

        Button removePropertyBtn = new Button( "Remove" );
        removePropertyBtn.addStyleName( "default" );
        removePropertyBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config != null )
                {
                    String fileName = fileTextField.getValue();
                    String propertyName = propertyNameTextField.getValue();
                    if ( Strings.isNullOrEmpty( fileName ) )
                    {
                        show( "Please, specify file name where property resides" );
                    }
                    else if ( Strings.isNullOrEmpty( propertyName ) )
                    {
                        show( "Please, specify property name to remove" );
                    }
                    else
                    {
                        UUID trackID = ZookeeperUI.getManager()
                            .removeProperty( config.getClusterName(), fileName, propertyName );
                        ProgressWindow window = new ProgressWindow( ZookeeperUI.getExecutor(), ZookeeperUI.getTracker(),
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
                }
                else
                {
                    show( "Please, select cluster" );
                }
            }
        } );
        customPropertyContent.addComponent( removePropertyBtn );

        Label propertyValueLabel = new Label( "Property Value" );
        customPropertyContent.addComponent( propertyValueLabel );
        final TextField propertyValueTextField = new TextField();
        customPropertyContent.addComponent( propertyValueTextField );
        Button addPropertyBtn = new Button( "Add" );
        addPropertyBtn.addStyleName( "default" );
        addPropertyBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config != null )
                {
                    String fileName = fileTextField.getValue();
                    String propertyName = propertyNameTextField.getValue();
                    String propertyValue = propertyValueTextField.getValue();
                    if ( Strings.isNullOrEmpty( fileName ) )
                    {
                        show( "Please, specify file name where property will be added" );
                    }
                    else if ( Strings.isNullOrEmpty( propertyName ) )
                    {
                        show( "Please, specify property name to add" );
                    }
                    else if ( Strings.isNullOrEmpty( propertyValue ) )
                    {
                        show( "Please, specify property value to set" );
                    }
                    else
                    {
                        UUID trackID = ZookeeperUI.getManager()
                            .addProperty( config.getClusterName(), fileName, propertyName, propertyValue );
                        ProgressWindow window = new ProgressWindow( ZookeeperUI.getExecutor(), ZookeeperUI.getTracker(),
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
                }
                else
                {
                    show( "Please, select cluster" );
                }
            }
        } );
        customPropertyContent.addComponent( addPropertyBtn );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( customPropertyContent, 0, 1 );
        contentRoot.addComponent( nodesTable, 0, 2, 0, 10 );
    }


    public static void checkNodesStatus( Table table )
    {
        for ( Object o : table.getItemIds() )
        {
            int rowId = ( Integer ) o;
            Item row = table.getItem( rowId );
            Button checkBtn = ( Button ) ( row.getItemProperty( "Check" ).getValue() );
            checkBtn.addStyleName( "default" );
            checkBtn.click();
        }
    }


    public static void startAllNodes( Table table )
    {
        for ( Object o : table.getItemIds() )
        {
            int rowId = ( Integer ) o;
            Item row = table.getItem( rowId );
            Button checkBtn = ( Button ) ( row.getItemProperty( "Start" ).getValue() );
            checkBtn.addStyleName( "default" );
            checkBtn.click();
        }
    }


    public static void stopAllNodes( Table table )
    {
        for ( Object o : table.getItemIds() )
        {
            int rowId = ( Integer ) o;
            Item row = table.getItem( rowId );
            Button checkBtn = ( Button ) ( row.getItemProperty( "Stop" ).getValue() );
            checkBtn.addStyleName( "default" );
            checkBtn.click();
        }
    }


    private Table createTableTemplate( String caption )
    {
        final Table table = new Table( caption );
        table.addContainerProperty( "Host", String.class, null );
        table.addContainerProperty( "Check", Button.class, null );
        table.addContainerProperty( "Start", Button.class, null );
        table.addContainerProperty( "Stop", Button.class, null );
        table.addContainerProperty( "Destroy", Button.class, null );
        table.addContainerProperty( "Status", Embedded.class, null );
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
                    Agent lxcAgent = ZookeeperUI.getAgentManager().getAgentByHostname( lxcHostname );
                    if ( lxcAgent != null )
                    {
                        TerminalWindow terminal = new TerminalWindow( Sets.newHashSet( lxcAgent ),
                            ZookeeperUI.getExecutor(), ZookeeperUI.getCommandRunner(), ZookeeperUI.getAgentManager() );
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
        List<Config> mongoClusterInfos = ZookeeperUI.getManager().getClusters();
        Config clusterInfo = ( Config ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if ( mongoClusterInfos != null && mongoClusterInfos.size() > 0 )
        {
            for ( Config mongoClusterInfo : mongoClusterInfos )
            {
                clusterCombo.addItem( mongoClusterInfo );
                clusterCombo.setItemCaption( mongoClusterInfo,
                    mongoClusterInfo.getClusterName() );
            }
            if ( clusterInfo != null )
            {
                for ( Config mongoClusterInfo : mongoClusterInfos )
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
                clusterCombo.setValue( mongoClusterInfos.iterator().next() );
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
            final Button checkBtn = new Button( "Check" );
            checkBtn.addStyleName( "default" );
            final Button startBtn = new Button( "Start" );
            startBtn.addStyleName( "default" );
            final Button stopBtn = new Button( "Stop" );
            stopBtn.addStyleName( "default" );
            final Button destroyBtn = new Button( "Destroy" );
            destroyBtn.addStyleName( "default" );
            final Embedded progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
            stopBtn.setEnabled( false );
            startBtn.setEnabled( false );
            progressIcon.setVisible( false );

            table.addItem( new Object[] {
                    agent.getHostname(),
                    checkBtn,
                    startBtn,
                    stopBtn,
                    destroyBtn,
                    progressIcon },
                null
            );

            checkBtn.addClickListener( new Button.ClickListener()
            {

                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    progressIcon.setVisible( true );
                    startBtn.setEnabled( false );
                    stopBtn.setEnabled( false );
                    destroyBtn.setEnabled( false );

                    ZookeeperUI.getExecutor()
                        .execute( new CheckTask( config.getClusterName(), agent.getHostname(), new CompleteEvent()
                        {

                            public void onComplete( NodeState state )
                            {
                                synchronized ( progressIcon )
                                {
                                    if ( state == NodeState.RUNNING )
                                    {
                                        stopBtn.setEnabled( true );
                                    }
                                    else if ( state == NodeState.STOPPED )
                                    {
                                        startBtn.setEnabled( true );
                                    }
                                    destroyBtn.setEnabled( true );
                                    progressIcon.setVisible( false );
                                }
                            }
                        } ) );
                }
            } );

            startBtn.addClickListener( new Button.ClickListener()
            {

                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    progressIcon.setVisible( true );
                    startBtn.setEnabled( false );
                    stopBtn.setEnabled( false );
                    destroyBtn.setEnabled( false );

                    ZookeeperUI.getExecutor()
                        .execute( new StartTask( config.getClusterName(), agent.getHostname(), new CompleteEvent()
                        {

                            public void onComplete( NodeState state )
                            {
                                synchronized ( progressIcon )
                                {
                                    if ( state == NodeState.RUNNING )
                                    {
                                        stopBtn.setEnabled( true );
                                    }
                                    else
                                    {
                                        startBtn.setEnabled( true );
                                    }
                                    destroyBtn.setEnabled( true );
                                    progressIcon.setVisible( false );
                                }
                            }
                        } ) );

                }
            } );

            stopBtn.addClickListener( new Button.ClickListener()
            {

                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    progressIcon.setVisible( true );
                    startBtn.setEnabled( false );
                    stopBtn.setEnabled( false );
                    destroyBtn.setEnabled( false );

                    ZookeeperUI.getExecutor()
                        .execute( new StopTask( config.getClusterName(), agent.getHostname(), new CompleteEvent()
                        {

                            public void onComplete( NodeState state )
                            {
                                synchronized ( progressIcon )
                                {
                                    if ( state == NodeState.STOPPED )
                                    {
                                        startBtn.setEnabled( true );
                                    }
                                    else
                                    {
                                        stopBtn.setEnabled( true );
                                    }
                                    destroyBtn.setEnabled( true );
                                    progressIcon.setVisible( false );
                                }
                            }
                        } ) );
                }
            } );

            destroyBtn.addClickListener( new Button.ClickListener()
            {

                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                        String.format( "Do you want to destroy the %s node?", agent.getHostname() ),
                        "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID trackID = ZookeeperUI.getManager()
                                .destroyNode( config.getClusterName(), agent.getHostname() );
                            ProgressWindow window = new ProgressWindow( ZookeeperUI.getExecutor(),
                                ZookeeperUI.getTracker(), trackID, Config.PRODUCT_KEY );
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
