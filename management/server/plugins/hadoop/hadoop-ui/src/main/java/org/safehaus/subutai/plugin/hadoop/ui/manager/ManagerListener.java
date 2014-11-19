/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.safehaus.subutai.plugin.hadoop.ui.manager;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.CompleteEvent;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.api.CheckDecommissionStatusTask;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.HadoopNodeOperationTask;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
import org.safehaus.subutai.server.ui.component.ProgressWindow;
import org.safehaus.subutai.server.ui.component.QuestionDialog;
import org.safehaus.subutai.server.ui.component.TerminalWindow;

import com.google.common.collect.Sets;
import com.vaadin.data.Item;
import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;


public class ManagerListener
{


    private static final Action ADD_ITEM_ACTION = new Action( "Add new node" );

    private Manager hadoopManager;


    public ManagerListener( Manager hadoopManager )
    {
        this.hadoopManager = hadoopManager;
    }


    protected ItemClickEvent.ItemClickListener getTableClickListener( final Table table )
    {
        return new ItemClickEvent.ItemClickListener()
        {
            @Override
            public void itemClick( ItemClickEvent event )
            {
                if ( event.isDoubleClick() )
                {
                    String containerId =
                            ( String ) table.getItem( event.getItemId() ).getItemProperty( Manager.HOST_COLUMN_CAPTION ).getValue();
                    ContainerHost containerHost = hadoopManager.getEnvironmentManager().getEnvironmentByUUID(
                            hadoopManager.getHadoopCluster().getEnvironmentId())
                                                               .getContainerHostByHostname( containerId );

                    if ( containerHost != null )
                    {
                        TerminalWindow terminal = new TerminalWindow( Sets.newHashSet( containerHost ) );
                        hadoopManager.getContentRoot().getUI().addWindow( terminal.getWindow() );
                    }
                    else
                    {
                        Notification.show( "Agent is not connected" );
                    }
                }
            }
        };
    }


    protected Button.ClickListener addNodeButtonListener()
    {
        return new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( hadoopManager.getHadoopCluster() != null )
                {
                    final QuestionDialog questionDialog =
                            new QuestionDialog<Integer>( ADD_ITEM_ACTION, "How many nodes do you want to add?",
                                    Integer.class, "Next", "Cancel" );
                    questionDialog.getOk().setId( "addNodeOk" );
                    questionDialog.getCancel().setId( "addNodeCancel" );
                    questionDialog.getInputField().setId( "addNodeInput" );
                    questionDialog.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( final Button.ClickEvent clickEvent )
                        {
                            ConfirmationDialog alert = new ConfirmationDialog( String.format(
                                    "Do you want to add " + questionDialog.getInputField().getValue()
                                            + " slave node to the %s cluster?",
                                    hadoopManager.getHadoopCluster().getClusterName() ), "Yes", "No" );
                            alert.getOk().addClickListener( new Button.ClickListener()
                            {
                                @Override
                                public void buttonClick( Button.ClickEvent clickEvent )
                                {
                                    UUID trackID = hadoopManager.getHadoop().addNode(
                                            hadoopManager.getHadoopCluster().getClusterName(),
                                            ( Integer ) questionDialog.getConvertedValue() );
                                    ProgressWindow window = new ProgressWindow( hadoopManager.getExecutorService(),
                                            hadoopManager.getTracker(), trackID, HadoopClusterConfig.PRODUCT_KEY );
                                    window.getWindow().addCloseListener( new Window.CloseListener()
                                    {
                                        @Override
                                        public void windowClose( Window.CloseEvent closeEvent )
                                        {
                                            hadoopManager.getCheckAllButton().click();
                                        }
                                    } );
                                    hadoopManager.getContentRoot().getUI().addWindow( window.getWindow() );
                                }
                            } );
                            if ( questionDialog.isConvertible( questionDialog.getInputField().getValue() ) )
                            {
                                hadoopManager.getContentRoot().getUI().addWindow( alert.getAlert() );
                            }
                        }
                    } );


                    hadoopManager.getContentRoot().getUI().addWindow( questionDialog.getAlert() );
                }
                else
                {
                    hadoopManager.show( "Please, select cluster" );
                }
            }
        };
    }


    protected Button.ClickListener destroyClusterButtonListener()
    {
        return new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                if ( hadoopManager.getHadoopCluster() != null )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to destroy the %s cluster?",
                                    hadoopManager.getHadoopCluster().getClusterName() ), "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            UUID trackID = hadoopManager.getHadoop().uninstallCluster(
                                    hadoopManager.getHadoopCluster().getClusterName() );
                            ProgressWindow window =
                                    new ProgressWindow( hadoopManager.getExecutorService(), hadoopManager.getTracker(),
                                            trackID, HadoopClusterConfig.PRODUCT_KEY );
                            window.getWindow().addCloseListener( new Window.CloseListener()
                            {
                                @Override
                                public void windowClose( Window.CloseEvent closeEvent )
                                {
                                    refreshClusterList();
                                }
                            } );
                            hadoopManager.getContentRoot().getUI().addWindow( window.getWindow() );
                        }
                    } );

                    hadoopManager.getContentRoot().getUI().addWindow( alert.getAlert() );
                }
                else
                {
                    hadoopManager.show( "Please, select cluster" );
                }
            }
        };
    }


    protected void refreshClusterList()
    {
        if ( hadoopManager.getHadoop() == null )
        {
            return;
        }
        List<HadoopClusterConfig> hadoopClusterList = hadoopManager.getHadoop().getClusters();
        HadoopClusterConfig clusterInfo = ( HadoopClusterConfig ) hadoopManager.getClusterList().getValue();
        hadoopManager.getClusterList().removeAllItems();
        if ( hadoopClusterList != null && hadoopClusterList.size() > 0 )
        {
            for ( HadoopClusterConfig hadoopCluster : hadoopClusterList )
            {
                if ( hadoopCluster.getNameNode() != null )
                {
                    hadoopManager.getClusterList().addItem( hadoopCluster );
                    hadoopManager.getClusterList().setItemCaption( hadoopCluster, hadoopCluster.getClusterName() );
                }
            }
            if ( clusterInfo != null )
            {
                for ( HadoopClusterConfig hadoopCluster : hadoopClusterList )
                {
                    if ( hadoopCluster.getClusterName().equals( clusterInfo.getClusterName() ) )
                    {
                        hadoopManager.getClusterList().setValue( hadoopCluster );
                        return;
                    }
                }
            }
            else
            {
                for ( HadoopClusterConfig hadoopCluster : hadoopClusterList )
                {
                    if ( hadoopCluster.getNameNode() != null )
                    {
                        hadoopManager.getClusterList().setValue( hadoopCluster );
                        return;
                    }
                }
            }
        }
    }


    protected Button.ClickListener slaveNodeDestroyButtonListener( final Item row )
    {
        final Agent agent = hadoopManager.getAgentByRow( row );
        final HorizontalLayout statusGroup = hadoopManager.getStatusLayout( row );
        final Label statusDecommission = hadoopManager.getStatusDecommissionLabel( statusGroup );

        return new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                String question = "Do you want to destroy the %s node?";
                if ( statusDecommission.getValue().toLowerCase().contains( NodeState.NORMAL.toString().toLowerCase() ) )
                {
                    question = "Do you want to destroy the node %s without decommissioning process?";
                }
                else if ( statusDecommission.getValue().toLowerCase()
                                            .contains( NodeState.DECOMMISSION_IN_PROGRESS.toString().toLowerCase() ) )
                {
                    question = "Do you want to destroy the node %s without decommissioning process?";
                }
                else if ( statusDecommission.getValue().toLowerCase()
                                            .contains( NodeState.DECOMMISSIONED.toString().toLowerCase() ) )
                {
                    question = "Do you want to destroy %s decommissioned node?";
                }

                ConfirmationDialog alert =
                        new ConfirmationDialog( String.format( question, agent.getHostname() ), "Yes", "No" );
                alert.getOk().addClickListener( new Button.ClickListener()
                {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent )
                    {
                        UUID trackID = hadoopManager.getHadoop().destroyNode( hadoopManager.getHadoopCluster(),
                                agent.getHostname() );
                        ProgressWindow window =
                                new ProgressWindow( hadoopManager.getExecutorService(), hadoopManager.getTracker(),
                                        trackID, HadoopClusterConfig.PRODUCT_KEY );
                        window.getWindow().addCloseListener( new Window.CloseListener()
                        {
                            @Override
                            public void windowClose( Window.CloseEvent closeEvent )
                            {
                                hadoopManager.getCheckAllButton().click();
                            }
                        } );
                        hadoopManager.getContentRoot().getUI().addWindow( window.getWindow() );
                    }
                } );

                hadoopManager.getContentRoot().getUI().addWindow( alert.getAlert() );
            }
        };
    }


    protected Button.ClickListener slaveNodeExcludeIncludeButtonListener( final Item row )
    {
        final Agent agent = hadoopManager.getAgentByRow( row );
        final HorizontalLayout availableOperationsLayout = hadoopManager.getAvailableOperationsLayout( row );
        final Button checkButton = hadoopManager.getCheckButton( availableOperationsLayout );
        final Button excludeIncludeNodeButton = hadoopManager.getExcludeIncludeButton( availableOperationsLayout );


        return new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {

                if ( excludeIncludeNodeButton.getCaption().equals( Manager.INCLUDE_BUTTON_CAPTION ) )
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to include the %s node?", agent.getHostname() ), "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            excludeIncludeNodeButton.setEnabled( false );
                            UUID trackID = hadoopManager.getHadoop().includeNode( hadoopManager.getHadoopCluster(),
                                    agent.getHostname() );
                            ProgressWindow window =
                                    new ProgressWindow( hadoopManager.getExecutorService(), hadoopManager.getTracker(),
                                            trackID, HadoopClusterConfig.PRODUCT_KEY );
                            window.getWindow().addCloseListener( new Window.CloseListener()
                            {
                                @Override
                                public void windowClose( Window.CloseEvent closeEvent )
                                {
                                    checkButton.click();
                                    excludeIncludeNodeButton.setEnabled( true );
                                }
                            } );
                            hadoopManager.getContentRoot().getUI().addWindow( window.getWindow() );
                        }
                    } );

                    hadoopManager.getContentRoot().getUI().addWindow( alert.getAlert() );
                }
                else
                {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to exclude the %s node?", agent.getHostname() ), "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener()
                    {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent )
                        {
                            excludeIncludeNodeButton.setEnabled( false );
                            UUID trackID = hadoopManager.getHadoop().excludeNode( hadoopManager.getHadoopCluster(),
                                    agent.getHostname() );
                            ProgressWindow window =
                                    new ProgressWindow( hadoopManager.getExecutorService(), hadoopManager.getTracker(),
                                            trackID, HadoopClusterConfig.PRODUCT_KEY );
                            window.getWindow().addCloseListener( new Window.CloseListener()
                            {
                                @Override
                                public void windowClose( Window.CloseEvent closeEvent )
                                {
                                    hadoopManager.getCheckAllButton().click();
                                    excludeIncludeNodeButton.setEnabled( true );
                                }
                            } );
                            hadoopManager.getContentRoot().getUI().addWindow( window.getWindow() );
                        }
                    } );

                    hadoopManager.getContentRoot().getUI().addWindow( alert.getAlert() );
                }
            }
        };
    }


    protected Button.ClickListener secondaryNameNodeCheckButtonListener( final Item row )
    {
        final Agent agent = hadoopManager.getAgentByRow( row );
        final ContainerHost containerHost = hadoopManager.getEnvironmentManager().
                getEnvironmentByUUID( hadoopManager.getHadoopCluster().getEnvironmentId() ).getContainerHostByUUID(
                agent.getUuid() );
        final String clusterName = hadoopManager.getHadoopCluster().getClusterName();
        final HorizontalLayout availableOperationsLayout = hadoopManager.getAvailableOperationsLayout( row );
        final HorizontalLayout statusGroupLayout = hadoopManager.getStatusLayout( row );
        final Button checkButton = hadoopManager.getCheckButton( availableOperationsLayout );
        final Label statusDatanode = hadoopManager.getStatusDatanodeLabel( statusGroupLayout );

        return new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                hadoopManager.enableProgressBar();
                checkButton.setEnabled( false );
                hadoopManager.getExecutorService().execute(
                        new HadoopNodeOperationTask( hadoopManager.getHadoop(), hadoopManager.getTracker(), clusterName,
                                containerHost, NodeOperationType.STATUS, NodeType.SECONDARY_NAMENODE, new CompleteEvent()
                        {

                            public void onComplete( NodeState state )
                            {
                                if ( state == NodeState.RUNNING )
                                {
                                    statusDatanode.setValue( "SecondaryNameNode Running" );
                                }
                                else if ( state == NodeState.STOPPED )
                                {
                                    statusDatanode.setValue( "SecondaryNameNode Stopped" );
                                }
                                else
                                {
                                    statusDatanode.setValue( "SecondaryNameNode Not Connected" );
                                }

                                checkButton.setEnabled( true );
                                hadoopManager.disableProgressBar();
                                enableCheckAllButton();
                            }
                        }, null ) );
            }
        };
    }


    private void enableCheckAllButton()
    {
        if ( hadoopManager.getProcessCount() == 0 )
        {
            hadoopManager.getCheckAllButton().setEnabled( true );
        }
    }


    protected Button.ClickListener secondaryNameNodeURLButtonListener( final Agent agent )
    {
        return new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                hadoopManager.getContentRoot().getUI().getPage()
                             .open( "http://" + agent.getListIP().get( 0 ) + ":50075", "SecondaryNameNode", false );
            }
        };
    }


    protected Button.ClickListener jobTrackerStartStopButtonListener( final Item row )
    {
        final Agent agent = hadoopManager.getAgentByRow( row );
        final ContainerHost containerHost = hadoopManager.getEnvironmentManager().
                getEnvironmentByUUID( hadoopManager.getHadoopCluster().getEnvironmentId() ).getContainerHostByUUID(
                agent.getUuid() );
        final String clusterName = hadoopManager.getHadoopCluster().getClusterName();
        final HorizontalLayout availableOperationsLayout = hadoopManager.getAvailableOperationsLayout( row );
        final Button startStopButton = hadoopManager.getStartStopButton( availableOperationsLayout );

        return new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                startStopButton.setEnabled( false );
                hadoopManager.enableProgressBar();
                boolean isRunning = startStopButton.getCaption().equals( Manager.STOP_JOBTRACKER_BUTTON_CAPTION );
                if ( !isRunning )
                {
                    startStopButton.setEnabled( false );
                    hadoopManager.getExecutorService().execute(
                            new HadoopNodeOperationTask( hadoopManager.getHadoop(), hadoopManager.getTracker(), clusterName,
                                    containerHost, NodeOperationType.START, NodeType.JOBTRACKER, new CompleteEvent()
                            {
                                public void onComplete( NodeState state )
                                {
                                    try
                                    {
                                        Thread.sleep( 1000 );
                                    }
                                    catch ( InterruptedException e )
                                    {
                                        hadoopManager.show( "Exception: " + e );
                                    }
                                    hadoopManager.disableProgressBar();
                                    hadoopManager.checkAllIfNoProcessRunning();
                                    startStopButton.setEnabled( true );
                                }
                            }, null ) );
                }
                else
                {
                    startStopButton.setEnabled( false );
                    hadoopManager.getExecutorService().execute(
                            new HadoopNodeOperationTask( hadoopManager.getHadoop(), hadoopManager.getTracker(), clusterName,
                                    containerHost, NodeOperationType.STOP, NodeType.JOBTRACKER, new CompleteEvent()
                            {
                                public void onComplete( NodeState state )
                                {
                                    hadoopManager.disableProgressBar();
                                    hadoopManager.checkAllIfNoProcessRunning();
                                    startStopButton.setEnabled( true );
                                }
                            }, null ) );
                }
            }
        };
    }


    protected Button.ClickListener jobTrackerCheckButtonListener( final Item row )
    {
        final Agent agent = hadoopManager.getAgentByRow( row );
        final ContainerHost containerHost = hadoopManager.getEnvironmentManager().
                getEnvironmentByUUID( hadoopManager.getHadoopCluster().getEnvironmentId() ).getContainerHostByUUID(
                agent.getUuid() );
        final String clusterName = hadoopManager.getHadoopCluster().getClusterName();
        final HorizontalLayout availableOperationsLayout = hadoopManager.getAvailableOperationsLayout( row );
        final HorizontalLayout statusGroupLayout = hadoopManager.getStatusLayout( row );
        final Button startStopButton = hadoopManager.getStartStopButton( availableOperationsLayout );
        final Button checkButton = hadoopManager.getCheckButton( availableOperationsLayout );
        final Label statusTaskTracker = hadoopManager.getStatusDatanodeLabel( statusGroupLayout );

        return new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                hadoopManager.enableProgressBar();
                startStopButton.setEnabled( false );
                checkButton.setEnabled( false );
                hadoopManager.getExecutorService().execute(
                        new HadoopNodeOperationTask( hadoopManager.getHadoop(), hadoopManager.getTracker(), clusterName,
                                containerHost, NodeOperationType.STATUS, NodeType.JOBTRACKER, new CompleteEvent()
                        {

                            public void onComplete( NodeState state )
                            {
                                if ( state == NodeState.RUNNING )
                                {
                                    statusTaskTracker.setValue( "JobTracker Running" );
                                    startStopButton.setCaption( Manager.STOP_JOBTRACKER_BUTTON_CAPTION );
                                    startStopButton.setEnabled( true );
                                }
                                else if ( state == NodeState.STOPPED )
                                {
                                    statusTaskTracker.setValue( "JobTracker Stopped" );
                                    startStopButton.setCaption( Manager.START_JOBTRACKER_BUTTON_CAPTION );
                                    startStopButton.setEnabled( true );
                                }
                                else
                                {
                                    statusTaskTracker.setValue( "JobTracker Not Connected" );
                                    startStopButton.setCaption( "Not connected" );
                                }

                                checkButton.setEnabled( true );
                                hadoopManager.disableProgressBar();
                                enableCheckAllButton();
                            }
                        }, null ) );
            }
        };
    }


    protected Button.ClickListener nameNodeCheckButtonListener( final Item row )
    {
        final Agent agent = hadoopManager.getAgentByRow( row );
        final ContainerHost containerHost = hadoopManager.getEnvironmentManager().
                getEnvironmentByUUID( hadoopManager.getHadoopCluster().getEnvironmentId() ).getContainerHostByUUID(
                agent.getUuid() );
        final String clusterName = hadoopManager.getHadoopCluster().getClusterName();
        final HorizontalLayout availableOperationsLayout = hadoopManager.getAvailableOperationsLayout( row );
        final HorizontalLayout statusGroupLayout = hadoopManager.getStatusLayout( row );
        final Button startStopButton = hadoopManager.getStartStopButton( availableOperationsLayout );
        final Button checkButton = hadoopManager.getCheckButton( availableOperationsLayout );
        final Label statusDatanode = hadoopManager.getStatusDatanodeLabel( statusGroupLayout );

        return new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                hadoopManager.enableProgressBar();
                checkButton.setEnabled( false );
                startStopButton.setEnabled( false );
                hadoopManager.getExecutorService().execute(
                        new HadoopNodeOperationTask( hadoopManager.getHadoop(), hadoopManager.getTracker(), clusterName,
                                containerHost, NodeOperationType.STATUS, NodeType.NAMENODE, new CompleteEvent()
                        {
                            public void onComplete( NodeState state )
                            {
                                if ( state == NodeState.RUNNING )
                                {
                                    statusDatanode.setValue( "Namenode Running" );
                                    startStopButton.setCaption( Manager.STOP_NAMENODE_BUTTON_CAPTION );
                                    startStopButton.setEnabled( true );
                                }
                                else if ( state == NodeState.STOPPED )
                                {
                                    statusDatanode.setValue( "Namenode Stopped" );
                                    startStopButton.setCaption( Manager.START_NAMENODE_BUTTON_CAPTION );
                                    startStopButton.setEnabled( true );
                                }
                                else
                                {
                                    statusDatanode.setValue( "Namenode Not Connected" );
                                    startStopButton.setCaption( "Not connected" );
                                }
                                checkButton.setEnabled( true );
                                hadoopManager.disableProgressBar();
                                enableCheckAllButton();
                            }
                        }, null ) );
            }
        };
    }


    protected Button.ClickListener nameNodeStartStopButtonListener( final Item row )
    {
        final Agent agent = hadoopManager.getAgentByRow( row );
        final ContainerHost containerHost = hadoopManager.getEnvironmentManager().
                getEnvironmentByUUID( hadoopManager.getHadoopCluster().getEnvironmentId() ).getContainerHostByUUID(
                agent.getUuid() );
        final String clusterName = hadoopManager.getHadoopCluster().getClusterName();
        final HorizontalLayout availableOperationsLayout = hadoopManager.getAvailableOperationsLayout( row );
        final Button startStopButton = hadoopManager.getStartStopButton( availableOperationsLayout );

        return new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                startStopButton.setEnabled( false );
                hadoopManager.enableProgressBar();
                boolean isRunning = startStopButton.getCaption().equals( Manager.STOP_NAMENODE_BUTTON_CAPTION );
                if ( !isRunning )
                {
                    hadoopManager.getExecutorService().execute(
                            new HadoopNodeOperationTask( hadoopManager.getHadoop(), hadoopManager.getTracker(), clusterName,
                                    containerHost, NodeOperationType.START, NodeType.NAMENODE, new CompleteEvent()
                            {

                                public void onComplete( NodeState state )
                                {
                                    try
                                    {
                                        Thread.sleep( 1000 );
                                    }
                                    catch ( InterruptedException e )
                                    {
                                        hadoopManager.show( "Exception: " + e );
                                    }
                                    hadoopManager.disableProgressBar();
                                    hadoopManager.checkAllIfNoProcessRunning();
                                    startStopButton.setEnabled( true );
                                }
                            }, null ) );
                }
                else
                {
                    hadoopManager.getExecutorService().execute(
                            new HadoopNodeOperationTask( hadoopManager.getHadoop(), hadoopManager.getTracker(), clusterName,
                                    containerHost, NodeOperationType.STOP, NodeType.NAMENODE, new CompleteEvent()
                            {

                                public void onComplete( NodeState state )
                                {

                                    hadoopManager.disableProgressBar();
                                    hadoopManager.checkAllIfNoProcessRunning();
                                    startStopButton.setEnabled( true );
                                }
                            }, null ) );
                }
            }
        };
    }


    protected Button.ClickListener nameNodeURLButtonListener( final Agent agent )
    {
        return new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent event )
            {
                hadoopManager.getContentRoot().getUI().getPage()
                             .open( "http://" + agent.getListIP().get( 0 ) + ":50070", "Namenode", false );
            }
        };
    }


    protected Button.ClickListener slaveNodeCheckButtonListener( final Item row )
    {
        final Agent agent = hadoopManager.getAgentByRow( row );
        final ContainerHost containerHost = hadoopManager.getEnvironmentManager().
                getEnvironmentByUUID( hadoopManager.getHadoopCluster().getEnvironmentId() ).getContainerHostByUUID(
                agent.getUuid() );
        final String clusterName = hadoopManager.getHadoopCluster().getClusterName();
        final HorizontalLayout availableOperationsLayout = hadoopManager.getAvailableOperationsLayout( row );
        final HorizontalLayout statusGroupLayout = hadoopManager.getStatusLayout( row );
        final Button checkButton = hadoopManager.getCheckButton( availableOperationsLayout );
        final Button excludeIncludeNodeButton = hadoopManager.getExcludeIncludeButton( availableOperationsLayout );
        final Button destroyButton = hadoopManager.getDestroyButton( availableOperationsLayout );
        final Label statusDatanode = hadoopManager.getStatusDatanodeLabel( statusGroupLayout );
        final Label statusTaskTracker = hadoopManager.getStatusTaskTrackerLabel( statusGroupLayout );

        return new Button.ClickListener()
        {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent )
            {

                if ( hadoopManager.getHadoop().getCluster( hadoopManager.getHadoopCluster().getClusterName() )
                                  .getBlockedAgentUUIDs().contains( agent.getUuid() ) )
                {
                    excludeIncludeNodeButton.setCaption( Manager.INCLUDE_BUTTON_CAPTION );
                }
                else
                {
                    excludeIncludeNodeButton.setCaption( Manager.EXCLUDE_BUTTON_CAPTION );
                }

                checkButton.setEnabled( false );
                excludeIncludeNodeButton.setEnabled( false );
                destroyButton.setEnabled( false );
                if ( hadoopManager.getHadoop().getCluster( hadoopManager.getHadoopCluster().getClusterName() )
                                  .isDataNode( agent.getUuid() ) )
                {
                    hadoopManager.enableProgressBar();
                    hadoopManager.getExecutorService().execute(
                            new HadoopNodeOperationTask( hadoopManager.getHadoop(), hadoopManager.getTracker(), clusterName,
                                    containerHost, NodeOperationType.STATUS, NodeType.DATANODE, new CompleteEvent()
                            {
                                public void onComplete( NodeState state )
                                {
                                    if ( state == NodeState.RUNNING )
                                    {
                                        statusDatanode.setValue( "Datanode Running" );
                                        excludeIncludeNodeButton.setEnabled( true );
                                    }
                                    else if ( state == NodeState.STOPPED )
                                    {
                                        statusDatanode.setValue( "Datanode Stopped" );
                                        excludeIncludeNodeButton.setEnabled( true );
                                    }
                                    else
                                    {
                                        statusDatanode.setValue( "Not connected" );
                                        excludeIncludeNodeButton.setCaption( "Not connected" );
                                        excludeIncludeNodeButton.setEnabled( false );
                                    }

                                    if ( hadoopManager.getCheckAllButton().isEnabled() )
                                    {
                                        checkDecommissioningStatus( row, checkButton );
                                    }
                                    else
                                    {
                                        executeSlaveNodeCheckButtonFinishCommands( row, checkButton );
                                    }
                                }
                            }, null ) );
                }
                if ( hadoopManager.getHadoop().getCluster( hadoopManager.getHadoopCluster().getClusterName() )
                                  .isTaskTracker( agent.getUuid() ) )
                {
                    hadoopManager.enableProgressBar();
                    hadoopManager.getExecutorService().execute(
                            new HadoopNodeOperationTask( hadoopManager.getHadoop(), hadoopManager.getTracker(), clusterName,
                                    containerHost, NodeOperationType.STATUS, NodeType.TASKTRACKER, new CompleteEvent()
                            {

                                public void onComplete( NodeState state )
                                {
                                    if ( state == NodeState.RUNNING )
                                    {
                                        statusTaskTracker.setValue( "Tasktracker Running" );
                                        excludeIncludeNodeButton.setEnabled( true );
                                    }
                                    else if ( state == NodeState.STOPPED )
                                    {
                                        statusTaskTracker.setValue( "Tasktracker Stopped" );
                                        excludeIncludeNodeButton.setEnabled( true );
                                    }
                                    else
                                    {
                                        statusTaskTracker.setValue( "Not connected" );
                                        excludeIncludeNodeButton.setCaption( "Not connected" );
                                        excludeIncludeNodeButton.setEnabled( false );
                                    }
                                    checkButton.setEnabled( true );
                                    hadoopManager.disableProgressBar();
                                    enableCheckAllButton();
                                }
                            }, null ) );
                }
            }
        };
    }


    private void executeSlaveNodeCheckButtonFinishCommands( Item row, Button checkButton )
    {
        final Agent agent = hadoopManager.getAgentByRow( row );
        final HorizontalLayout availableOperationsLayout = hadoopManager.getAvailableOperationsLayout( row );
        final HorizontalLayout statusGroupLayout = hadoopManager.getStatusLayout( row );
        final Button destroyButton = hadoopManager.getDestroyButton( availableOperationsLayout );
        final Label statusDecommission = hadoopManager.getStatusDecommissionLabel( statusGroupLayout );
        if ( agent != null )
        {
            statusDecommission.setValue( Manager.DECOMMISSION_STATUS_CAPTION + hadoopManager
                    .getDecommissionStatus( hadoopManager.getDecommissionStatus(), agent ) );
            checkButton.setEnabled( true );
            destroyButton.setEnabled( true );
            hadoopManager.disableProgressBar();
            if ( hadoopManager.getProcessCount() == 0 )
            {
                enableCheckAllButton();
                checkButton.setEnabled( true );
            }
        }
        else
        {
            statusDecommission.setValue( Manager.DECOMMISSION_STATUS_CAPTION + NodeState.UNKNOWN );
            checkButton.setEnabled( true );
            destroyButton.setEnabled( true );
            hadoopManager.disableProgressBar();
            if ( hadoopManager.getProcessCount() == 0 )
            {
                enableCheckAllButton();
                checkButton.setEnabled( true );
            }
        }
    }


    private void checkDecommissioningStatus( final Item row, final Button checkButton )
    {
        hadoopManager.getExecutorService().execute(
                new CheckDecommissionStatusTask( hadoopManager.getHadoop(), hadoopManager.getTracker(),
                        hadoopManager.getHadoopCluster(),
                        new org.safehaus.subutai.plugin.hadoop.api.CompleteEvent()
                        {
                            public void onComplete( String operationLog )
                            {
                                hadoopManager.setDecommissionStatus( operationLog );
                                executeSlaveNodeCheckButtonFinishCommands( row, checkButton );
                            }
                        }, null ) );
    }


    public Button.ClickListener checkAllButtonListener( final Button checkAllButton )
    {
        return new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                refreshClusterList();
                if ( hadoopManager.getClusterList().getValue() != null )
                {
                    checkAllButton.setEnabled( false );
                    checkAllStatuses();
                }
            }
        };
    }


    private void checkAllStatuses()
    {
        hadoopManager.enableProgressBar();
        hadoopManager.getExecutorService().execute(
                new CheckDecommissionStatusTask( hadoopManager.getHadoop(), hadoopManager.getTracker(),
                        hadoopManager.getHadoopCluster(),
                        new org.safehaus.subutai.plugin.hadoop.api.CompleteEvent()
                        {
                            public void onComplete( String operationLog )
                            {
                                hadoopManager.setDecommissionStatus( operationLog );
                                checkNodesStatus( hadoopManager.getMasterNodesTable() );
                                checkNodesStatus( hadoopManager.getSlaveNodesTable() );
                                hadoopManager.disableProgressBar();
                                enableCheckAllButton();
                            }
                        }, null ) );
    }


    private void checkNodesStatus( Table table )
    {
        if ( table != null )
        {
            for ( Object o : table.getItemIds() )
            {
                int rowId = ( Integer ) o;
                Item row = table.getItem( rowId );
                HorizontalLayout availableOperationsLayout = hadoopManager.getAvailableOperationsLayout( row );
                if ( availableOperationsLayout != null )
                {
                    Button checkButton = hadoopManager.getCheckButton( availableOperationsLayout );
                    if ( checkButton != null )
                    {
                        checkButton.click();
                    }
                }
            }
        }
    }
}
