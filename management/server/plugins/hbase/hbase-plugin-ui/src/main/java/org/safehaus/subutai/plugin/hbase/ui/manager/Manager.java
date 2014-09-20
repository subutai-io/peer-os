/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hbase.ui.manager;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;
import org.safehaus.subutai.plugin.hbase.api.HBaseType;
import org.safehaus.subutai.plugin.hbase.ui.HBaseUI;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.TerminalWindow;

import com.google.common.collect.Sets;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


/**
 * @author dilshat
 */
public class Manager
{

    private final VerticalLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table masterTable;
    private final Table regionTable;
    private final Table quorumTable;
    private final Table bmasterTable;
    private HBaseClusterConfig config;
    private HBaseUI hBaseUI;


    public Manager( final HBaseUI hBaseUI )
    {

        this.hBaseUI = hBaseUI;
        contentRoot = new VerticalLayout();
        contentRoot.setSpacing( true );
        contentRoot.setSizeFull();

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();

        contentRoot.addComponent( content );
        contentRoot.setComponentAlignment( content, Alignment.TOP_CENTER );
        contentRoot.setMargin( true );

        //tables go here
        masterTable = createTableTemplate( "Master" );
        regionTable = createTableTemplate( "Region" );
        quorumTable = createTableTemplate( "Quorum" );
        bmasterTable = createTableTemplate( "Backup master" );
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
                Object value = event.getProperty().getValue();
                config = value != null ? ( HBaseClusterConfig ) value : null;
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

        Button startClustersBtn = new Button( "Start cluster" );
        startClustersBtn.addStyleName( "default" );
        startClustersBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config != null )
                {
                    UUID trackID = hBaseUI.getHbaseManager().startCluster( config.getClusterName() );
                    ProgressWindow window = new ProgressWindow( hBaseUI.getExecutor(), hBaseUI.getTracker(), trackID,
                            HBaseClusterConfig.PRODUCT_KEY );
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
                else
                {
                    show( "Please, select cluster" );
                }
            }
        } );

        controlsContent.addComponent( startClustersBtn );

        Button stopClustersBtn = new Button( "Stop cluster" );
        stopClustersBtn.addStyleName( "default" );
        stopClustersBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config != null )
                {
                    UUID trackID = hBaseUI.getHbaseManager().stopCluster( config.getClusterName() );
                    ProgressWindow window = new ProgressWindow( hBaseUI.getExecutor(), hBaseUI.getTracker(), trackID,
                            HBaseClusterConfig.PRODUCT_KEY );
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
                else
                {
                    show( "Please, select cluster" );
                }
            }
        } );

        controlsContent.addComponent( stopClustersBtn );

        Button checkClustersBtn = new Button( "Check cluster" );
        checkClustersBtn.addStyleName( "default" );
        checkClustersBtn.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( config != null )
                {
                    UUID trackID = hBaseUI.getHbaseManager().checkCluster( config.getClusterName() );
                    ProgressWindow window = new ProgressWindow( hBaseUI.getExecutor(), hBaseUI.getTracker(), trackID,
                            HBaseClusterConfig.PRODUCT_KEY );
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
                else
                {
                    show( "Please, select cluster" );
                }
            }
        } );

        controlsContent.addComponent( checkClustersBtn );

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
                            String.format( "Do you want to add node to the %s cluster?", config.getClusterName() ),
                            "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID trackID = hBaseUI.getHbaseManager().uninstallCluster( config.getClusterName() );
                            ProgressWindow window =
                                    new ProgressWindow( hBaseUI.getExecutor(), hBaseUI.getTracker(), trackID,
                                            HBaseClusterConfig.PRODUCT_KEY );
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
        content.addComponent( controlsContent );
        content.addComponent( masterTable );
        content.addComponent( regionTable );
        content.addComponent( quorumTable );
        content.addComponent( bmasterTable );
    }


    private void refreshUI()
    {
        if ( config != null )
        {
            populateTable( quorumTable, config.getQuorum(), HBaseType.HQuorumPeer );
            populateTable( regionTable, config.getRegion(), HBaseType.HRegionServer );

            Set<String> masterSet = new HashSet<>();
            masterSet.add( config.getMaster() );
            populateMasterTable( masterTable, masterSet, HBaseType.HMaster );

            Set<String> bmasterSet = new HashSet<>();
            bmasterSet.add( config.getBackupMasters() );
            populateTable( bmasterTable, bmasterSet, HBaseType.BackupMaster );
        }
        else
        {
            regionTable.removeAllItems();
            quorumTable.removeAllItems();
            bmasterTable.removeAllItems();
            masterTable.removeAllItems();
        }
    }


    private void populateMasterTable( final Table table, Set<String> agents, final HBaseType type )
    {

        table.removeAllItems();

        for ( final String hostname : agents )
        {
            final Embedded progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
            progressIcon.setVisible( false );

            Agent a = hBaseUI.getAgentManager().getAgentByHostname( hostname );
            if ( a == null )
            {
                continue;
            }

            final Object rowId = table.addItem( new Object[] {
                    a.getHostname(), type, progressIcon
            }, null );
        }
    }


    private void populateTable( final Table table, Set<String> agents, final HBaseType type )
    {

        table.removeAllItems();

        for ( final String hostname : agents )
        {
            final Embedded progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
            progressIcon.setVisible( false );

            Agent a = hBaseUI.getAgentManager().getAgentByHostname( hostname );
            if ( a == null )
            {
                continue;
            }

            final Object rowId = table.addItem( new Object[] {
                    a.getHostname(), type, progressIcon
            }, null );
        }
    }


    public void refreshClustersInfo()
    {
        List<HBaseClusterConfig> clusters = hBaseUI.getHbaseManager().getClusters();
        HBaseClusterConfig clusterInfo = ( HBaseClusterConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if ( clusters != null && clusters.size() > 0 )
        {
            for ( HBaseClusterConfig info : clusters )
            {
                clusterCombo.addItem( info );
                clusterCombo.setItemCaption( info, info.getClusterName() );
            }
            if ( clusterInfo != null )
            {
                for ( HBaseClusterConfig c : clusters )
                {
                    if ( c.getClusterName().equals( clusterInfo.getClusterName() ) )
                    {
                        clusterCombo.setValue( c );
                        return;
                    }
                }
            }
            else
            {
                clusterCombo.setValue( clusters.iterator().next() );
            }
        }
    }


    private Table createTableTemplate( String caption )
    {
        final Table table = new Table( caption );
        table.addContainerProperty( "Host", String.class, null );
        table.addContainerProperty( "Type", HBaseType.class, null );
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
                    String lxcHostname =
                            ( String ) table.getItem( event.getItemId() ).getItemProperty( "Host" ).getValue();
                    Agent lxcAgent = hBaseUI.getAgentManager().getAgentByHostname( lxcHostname );
                    if ( lxcAgent != null )
                    {
                        TerminalWindow terminal =
                                new TerminalWindow( Sets.newHashSet( lxcAgent ), hBaseUI.getExecutor(),
                                        hBaseUI.getCommandRunner(), hBaseUI.getAgentManager() );
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


    private void show( String notification )
    {
        Notification.show( notification );
    }


    public static void checkNodesStatus( Table table )
    {
        for ( Object o : table.getItemIds() )
        {
            int rowId = ( Integer ) o;
            Item row = table.getItem( rowId );
            Button checkBtn = ( Button ) ( row.getItemProperty( "Check" ).getValue() );
            checkBtn.click();
        }
    }


    public Component getContent()
    {
        return contentRoot;
    }
}
