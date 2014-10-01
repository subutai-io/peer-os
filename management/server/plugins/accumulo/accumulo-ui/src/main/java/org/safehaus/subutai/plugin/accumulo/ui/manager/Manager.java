/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.accumulo.ui.manager;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.accumulo.api.Accumulo;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.api.NodeType;
import org.safehaus.subutai.plugin.accumulo.ui.common.UiUtil;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
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
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;


public class Manager
{

    private final GridLayout contentRoot;
    private final ComboBox clusterCombo;
    private final Table mastersTable;
    private final Table tracersTable;
    private final Table slavesTable;
    private final Pattern masterPattern = Pattern.compile( ".*(Master.+?g).*" );
    private final Pattern gcPattern = Pattern.compile( ".*(GC.+?g).*" );
    private final Pattern monitorPattern = Pattern.compile( ".*(Monitor.+?g).*" );
    private final Pattern tracerPattern = Pattern.compile( ".*(Tracer.+?g).*" );
    private final Pattern loggerPattern = Pattern.compile( ".*(Logger.+?g).*" );
    private final Pattern tabletServerPattern = Pattern.compile( ".*(Tablet Server.+?g).*" );
    private final Accumulo accumulo;
    private final Hadoop hadoop;
    private final Tracker tracker;
    private final ExecutorService executorService;
    private AccumuloClusterConfig accumuloClusterConfig;


    public Manager( final ExecutorService executorService, ServiceLocator serviceLocator ) throws NamingException
    {

        this.executorService = executorService;
        this.accumulo = serviceLocator.getService( Accumulo.class );
        this.hadoop = serviceLocator.getService( Hadoop.class );
        this.tracker = serviceLocator.getService( Tracker.class );
        AgentManager agentManager = serviceLocator.getService( AgentManager.class );
        CommandRunner commandRunner = serviceLocator.getService( CommandRunner.class );

        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 17 );
        contentRoot.setColumns( 1 );

        //tables go here
        mastersTable = UiUtil.createTableTemplate( "Masters", false, agentManager, commandRunner, executorService );
        tracersTable = UiUtil.createTableTemplate( "Tracers", true, agentManager, commandRunner, executorService );
        slavesTable = UiUtil.createTableTemplate( "Slaves", true, agentManager, commandRunner, executorService );
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
                accumuloClusterConfig = ( AccumuloClusterConfig ) event.getProperty().getValue();
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
                checkNodesStatus( mastersTable );
                checkNodesStatus( slavesTable );
                checkNodesStatus( tracersTable );
            }
        } );
        controlsContent.addComponent( checkAllBtn );

        Button startClusterBtn = new Button( "Start cluster" );
        startClusterBtn.addStyleName( "default" );
        startClusterBtn.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                UUID trackID = accumulo.startCluster( accumuloClusterConfig.getClusterName() );
                ProgressWindow window =
                        new ProgressWindow( executorService, tracker, trackID, AccumuloClusterConfig.PRODUCT_KEY );
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
        controlsContent.addComponent( startClusterBtn );

        Button stopClusterBtn = new Button( "Stop cluster" );
        stopClusterBtn.addStyleName( "default" );
        stopClusterBtn.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                UUID trackID = accumulo.stopCluster( accumuloClusterConfig.getClusterName() );
                ProgressWindow window =
                        new ProgressWindow( executorService, tracker, trackID, AccumuloClusterConfig.PRODUCT_KEY );
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
        controlsContent.addComponent( stopClusterBtn );

        Button destroyClusterBtn = new Button( "Destroy cluster" );
        destroyClusterBtn.addStyleName( "default" );
        destroyClusterBtn.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                if ( accumuloClusterConfig != null )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to destroy the %s cluster?",
                                    accumuloClusterConfig.getClusterName() ), "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID trackID = accumulo.uninstallCluster( accumuloClusterConfig.getClusterName() );
                            ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                    AccumuloClusterConfig.PRODUCT_KEY );
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
                    Notification.show( "Please, select cluster" );
                }
            }
        } );
        controlsContent.addComponent( destroyClusterBtn );

        Button addTracerBtn = new Button( "Add Tracer" );
        addTracerBtn.addStyleName( "default" );
        addTracerBtn.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                if ( accumuloClusterConfig != null )
                {

                    HadoopClusterConfig hadoopConfig = hadoop.getCluster( accumuloClusterConfig.getClusterName() );

                    if ( hadoopConfig != null )
                    {
                        Set<Agent> availableNodes = new HashSet<>( hadoopConfig.getAllNodes() );
                        availableNodes.removeAll( accumuloClusterConfig.getTracers() );
                        if ( availableNodes.isEmpty() )
                        {
                            Notification.show( "All Hadoop nodes already have tracers installed" );
                            return;
                        }

                        AddNodeWindow addNodeWindow =
                                new AddNodeWindow( accumulo, executorService, tracker, accumuloClusterConfig,
                                        availableNodes, NodeType.TRACER );
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
                        Notification.show( String
                                .format( "Hadoop cluster %s not found", accumuloClusterConfig.getClusterName() ) );
                    }
                }
                else
                {
                    Notification.show( "Please, select cluster" );
                }
            }
        } );
        controlsContent.addComponent( addTracerBtn );

        Button addSlaveBtn = new Button( "Add Slave" );
        addSlaveBtn.addStyleName( "default" );
        addSlaveBtn.addClickListener( new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                if ( accumuloClusterConfig != null )
                {

                    HadoopClusterConfig hadoopConfig = hadoop.getCluster( accumuloClusterConfig.getClusterName() );
                    if ( hadoopConfig != null )
                    {
                        Set<Agent> availableNodes = new HashSet<>( hadoopConfig.getAllNodes() );
                        availableNodes.removeAll( accumuloClusterConfig.getSlaves() );
                        if ( availableNodes.isEmpty() )
                        {
                            Notification.show( "All Hadoop nodes already have slaves installed" );
                            return;
                        }

                        AddNodeWindow addNodeWindow =
                                new AddNodeWindow( accumulo, executorService, tracker, accumuloClusterConfig,
                                        availableNodes, NodeType.LOGGER );
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
                        Notification.show( String
                                .format( "Hadoop cluster %s not found", accumuloClusterConfig.getClusterName() ) );
                    }
                }
                else
                {
                    Notification.show( "Please, select cluster" );
                }
            }
        } );
        controlsContent.addComponent( addSlaveBtn );

        HorizontalLayout customPropertyContent = new HorizontalLayout();
        customPropertyContent.setSpacing( true );

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
                if ( accumuloClusterConfig != null )
                {
                    String propertyName = propertyNameTextField.getValue();
                    if ( Strings.isNullOrEmpty( propertyName ) )
                    {
                        Notification.show( "Please, specify property name to remove" );
                    }
                    else
                    {
                        UUID trackID = accumulo.removeProperty( accumuloClusterConfig.getClusterName(), propertyName );

                        ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                AccumuloClusterConfig.PRODUCT_KEY );
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
                    Notification.show( "Please, select cluster" );
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
                if ( accumuloClusterConfig != null )
                {
                    String propertyName = propertyNameTextField.getValue();
                    String propertyValue = propertyValueTextField.getValue();
                    if ( Strings.isNullOrEmpty( propertyName ) )
                    {
                        Notification.show( "Please, specify property name to add" );
                    }
                    else if ( Strings.isNullOrEmpty( propertyValue ) )
                    {
                        Notification.show( "Please, specify property name to set" );
                    }
                    else
                    {
                        UUID trackID = accumulo.addProperty( accumuloClusterConfig.getClusterName(), propertyName,
                                propertyValue );

                        ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                AccumuloClusterConfig.PRODUCT_KEY );
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
                    Notification.show( "Please, select cluster" );
                }
            }
        } );
        customPropertyContent.addComponent( addPropertyBtn );

        contentRoot.addComponent( controlsContent, 0, 0 );
        contentRoot.addComponent( customPropertyContent, 0, 1 );
        contentRoot.addComponent( mastersTable, 0, 2, 0, 6 );
        contentRoot.addComponent( tracersTable, 0, 7, 0, 11 );
        contentRoot.addComponent( slavesTable, 0, 12, 0, 16 );
    }


    private void refreshUI()
    {
        if ( accumuloClusterConfig != null )
        {
            populateTable( slavesTable, new ArrayList<>( accumuloClusterConfig.getSlaves() ), false );
            populateTable( tracersTable, new ArrayList<>( accumuloClusterConfig.getTracers() ), false );
            List<Agent> masters = new ArrayList<>();
            masters.add( accumuloClusterConfig.getMasterNode() );
            masters.add( accumuloClusterConfig.getGcNode() );
            masters.add( accumuloClusterConfig.getMonitor() );
            populateTable( mastersTable, masters, true );
        }
        else
        {
            slavesTable.removeAllItems();
            tracersTable.removeAllItems();
            mastersTable.removeAllItems();
        }
    }


    private void populateTable( final Table table, List<Agent> agents, final boolean masters )
    {

        table.removeAllItems();

        int i = 0;
        for ( final Agent agent : agents )
        {
            i++;
            final Button checkBtn = new Button( "Check" );
            final Button destroyBtn = new Button( "Destroy" );
            final Embedded progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
            final Label resultHolder = new Label();
            destroyBtn.setEnabled( false );
            progressIcon.setVisible( false );

            table.addItem( masters ? new Object[] {
                    ( i == 1 ? UiUtil.MASTER_PREFIX : i == 2 ? UiUtil.GC_PREFIX : UiUtil.MONITOR_PREFIX ) + agent
                            .getHostname(), checkBtn, resultHolder, progressIcon
            } : new Object[] {
                    agent.getHostname(), checkBtn, destroyBtn, resultHolder, progressIcon
            }, null );

            checkBtn.addClickListener( new Button.ClickListener()
            {

                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    progressIcon.setVisible( true );

                    executorService.execute( new CheckTask( accumulo, tracker, accumuloClusterConfig.getClusterName(),
                            agent.getHostname(), new CompleteEvent()
                    {

                        public void onComplete( String result )
                        {
                            synchronized ( progressIcon )
                            {
                                if ( masters )
                                {
                                    resultHolder.setValue( parseMastersState( result ) );
                                }
                                else if ( table == tracersTable )
                                {
                                    resultHolder.setValue( parseTracersState( result ) );
                                }
                                else if ( table == slavesTable )
                                {
                                    resultHolder.setValue( parseSlavesState( result ) );
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
                            String.format( "Do you want to destroy the %s node?", agent.getHostname() ), "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID trackID =
                                    accumulo.destroyNode( accumuloClusterConfig.getClusterName(), agent.getHostname(),
                                            table == tracersTable ? NodeType.TRACER : NodeType.LOGGER );

                            ProgressWindow window = new ProgressWindow( executorService, tracker, trackID,
                                    AccumuloClusterConfig.PRODUCT_KEY );
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


    public void refreshClustersInfo()
    {
        List<AccumuloClusterConfig> mongoClusterInfos = accumulo.getClusters();
        AccumuloClusterConfig clusterInfo = ( AccumuloClusterConfig ) clusterCombo.getValue();
        clusterCombo.removeAllItems();
        if ( mongoClusterInfos != null && !mongoClusterInfos.isEmpty() )
        {
            for ( AccumuloClusterConfig mongoClusterInfo : mongoClusterInfos )
            {
                clusterCombo.addItem( mongoClusterInfo );
                clusterCombo.setItemCaption( mongoClusterInfo, mongoClusterInfo.getClusterName() );
            }
            if ( clusterInfo != null )
            {
                for ( AccumuloClusterConfig mongoClusterInfo : mongoClusterInfos )
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


    private String parseMastersState( String result )
    {
        StringBuilder parsedResult = new StringBuilder();
        Matcher masterMatcher = masterPattern.matcher( result );
        if ( masterMatcher.find() )
        {
            parsedResult.append( masterMatcher.group( 1 ) ).append( " " );
        }
        Matcher gcMatcher = gcPattern.matcher( result );
        if ( gcMatcher.find() )
        {
            parsedResult.append( gcMatcher.group( 1 ) ).append( " " );
        }
        Matcher monitorMatcher = monitorPattern.matcher( result );
        if ( monitorMatcher.find() )
        {
            parsedResult.append( monitorMatcher.group( 1 ) ).append( " " );
        }

        return parsedResult.toString();
    }


    private String parseTracersState( String result )
    {
        StringBuilder parsedResult = new StringBuilder();
        Matcher tracersMatcher = tracerPattern.matcher( result );
        if ( tracersMatcher.find() )
        {
            parsedResult.append( tracersMatcher.group( 1 ) ).append( " " );
        }

        return parsedResult.toString();
    }


    private String parseSlavesState( String result )
    {
        StringBuilder parsedResult = new StringBuilder();
        Matcher loggersMatcher = loggerPattern.matcher( result );
        if ( loggersMatcher.find() )
        {
            parsedResult.append( loggersMatcher.group( 1 ) ).append( " " );
        }
        Matcher tablerServersMatcher = tabletServerPattern.matcher( result );
        if ( tablerServersMatcher.find() )
        {
            parsedResult.append( tablerServersMatcher.group( 1 ) ).append( " " );
        }

        return parsedResult.toString();
    }


    public static void checkNodesStatus( Table table )
    {
        UiUtil.clickAllButtonsInTable( table, "Check" );
    }


    public Component getContent()
    {
        return contentRoot;
    }
}
