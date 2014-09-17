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
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.ui.HadoopUI;
import org.safehaus.subutai.plugin.hadoop.ui.manager.components.CheckDecommissionStatusTask;
import org.safehaus.subutai.plugin.hadoop.ui.manager.components.CheckTask;
import org.safehaus.subutai.plugin.hadoop.ui.manager.components.StartTask;
import org.safehaus.subutai.plugin.hadoop.ui.manager.components.StopTask;
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
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;


public class ManagerListener {



    private static final Action ADD_ITEM_ACTION = new Action( "Add new node" );

    private Manager hadoopManager;

    public ManagerListener( Manager hadoopManager ) {
        this.hadoopManager = hadoopManager;
    }



    // Click Listeners


    protected ItemClickEvent.ItemClickListener getTableClickListener( final Table table ) {
        return new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick( ItemClickEvent event ) {
                if ( event.isDoubleClick() ) {
                    String lxcHostname =
                            ( String ) table.getItem( event.getItemId() ).getItemProperty( "Host" ).getValue();
                    Agent lxcAgent = HadoopUI.getAgentManager().getAgentByHostname( lxcHostname );
                    if ( lxcAgent != null ) {
                        TerminalWindow terminal =
                                new TerminalWindow( Sets.newHashSet( lxcAgent ), HadoopUI.getExecutor(),
                                        HadoopUI.getCommandRunner(), HadoopUI.getAgentManager() );
                        hadoopManager.contentRoot.getUI().addWindow( terminal.getWindow() );
                    }
                    else {
                        hadoopManager.show( "Agent is not connected for" + lxcHostname );
                    }
                }
            }
        };
    }

    protected Button.ClickListener addNodeButtonListener() {

        return  new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                if ( hadoopManager.hadoopCluster != null ) {
                    final QuestionDialog questionDialog = new QuestionDialog<Integer>( ADD_ITEM_ACTION ,
                            "How many nodes do you want to add?",
                            Integer.class, "Next","Cancel");
                    questionDialog.getOk().addClickListener( new Button.ClickListener() {
                        @Override
                        public void buttonClick( final Button.ClickEvent clickEvent ) {
                            ConfirmationDialog alert = new ConfirmationDialog(
                                    String.format( "Do you want to add " + questionDialog.getInputField() + " slave node to the %s cluster?",
                                            hadoopManager.hadoopCluster.getClusterName() ), "Yes", "No" );
                            alert.getOk().addClickListener( new Button.ClickListener() {
                                @Override
                                public void buttonClick( Button.ClickEvent clickEvent ) {
                                    UUID trackID = HadoopUI.getHadoopManager().addNode(
                                            hadoopManager.hadoopCluster.getClusterName(),
                                            (Integer) questionDialog.getConvertedValue() );
                                    ProgressWindow window =
                                            new ProgressWindow( HadoopUI.getExecutor(), HadoopUI.getTracker(), trackID,
                                                    HadoopClusterConfig.PRODUCT_KEY );
                                    window.getWindow().addCloseListener( new Window.CloseListener() {
                                        @Override
                                        public void windowClose( Window.CloseEvent closeEvent ) {
                                            hadoopManager.checkAllButton.click();
                                        }
                                    } );
                                    hadoopManager.contentRoot.getUI().addWindow( window.getWindow() );
                                }
                            } );
                            if ( questionDialog.isConvertible( questionDialog.getInputField().getValue() ) ) {
                                hadoopManager.contentRoot.getUI().addWindow( alert.getAlert() );
                            }

                        }  });


                    hadoopManager.contentRoot.getUI().addWindow( questionDialog.getAlert() );
                }
                else {
                    hadoopManager.show( "Please, select cluster" );
                }
            }
        };
    }


    protected Button.ClickListener destroyClusterButtonListener() {

        return new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                if ( hadoopManager.hadoopCluster != null ) {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to destroy the %s cluster?", hadoopManager.hadoopCluster.getClusterName() ),
                            "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener() {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent ) {
                            UUID trackID =
                                    HadoopUI.getHadoopManager().uninstallCluster( hadoopManager.hadoopCluster.getClusterName() );
                            ProgressWindow window =
                                    new ProgressWindow( HadoopUI.getExecutor(), HadoopUI.getTracker(), trackID,
                                            HadoopClusterConfig.PRODUCT_KEY );
                            window.getWindow().addCloseListener( new Window.CloseListener() {
                                @Override
                                public void windowClose( Window.CloseEvent closeEvent ) {
                                    refreshClusterList();
                                }
                            } );
                            hadoopManager.contentRoot.getUI().addWindow( window.getWindow() );
                        }
                    } );

                    hadoopManager.contentRoot.getUI().addWindow( alert.getAlert() );
                }
                else {
                    hadoopManager.show( "Please, select cluster" );
                }
            }
        } ;
    }


    protected Button.ClickListener slaveNodeDestroyButtonListener( final Item row ) {
        final Agent agent = hadoopManager.getAgentByRow( row );
        final HorizontalLayout availableOperationsLayout = hadoopManager.getAvailableOperationsLayout( row );
        final HorizontalLayout statusGroup = hadoopManager.getStatusGroupByRow( row );
        final Label statusDecommission = hadoopManager.getStatusDecommissionLabel( statusGroup );


//        if ( agent == null )
//            return new Button.ClickListener() {
//                @Override
//                public void buttonClick( final Button.ClickEvent event ) {
//
//                }
//            };

        return new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                String question = "Do you want to destroy the %s node?";
                if ( statusDecommission.getValue().toLowerCase().contains(
                        NodeState.NORMAL.toString().toLowerCase() ) ) {
                    question = "Do you want to destroy the node %s without decommissioning process?";
                }
                else if ( statusDecommission.getValue().toLowerCase().contains
                        ( NodeState.DECOMMISSION_IN_PROGRESS.toString().toLowerCase() ) ) {
                    question = "Do you want to destroy the node %s without decommissioning process?";
                }
                else if ( statusDecommission.getValue().toLowerCase().contains
                        ( NodeState.DECOMMISSIONED.toString().toLowerCase() ) ){
                    question = "Do you want to destroy %s decommissioned node?";
                }

                ConfirmationDialog alert = new ConfirmationDialog( String.format( question, agent.getHostname() ), "Yes", "No" );
                alert.getOk().addClickListener( new Button.ClickListener() {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent ) {
                        UUID trackID = HadoopUI.getHadoopManager().destroyNode( hadoopManager.hadoopCluster, agent );
                        ProgressWindow window =
                                new ProgressWindow( HadoopUI.getExecutor(), HadoopUI.getTracker(), trackID,
                                        HadoopClusterConfig.PRODUCT_KEY );
                        window.getWindow().addCloseListener( new Window.CloseListener() {
                            @Override
                            public void windowClose( Window.CloseEvent closeEvent ) {
                                hadoopManager.checkAllButton.click();
                            }
                        } );
                        hadoopManager.contentRoot.getUI().addWindow( window.getWindow() );
                    }
                } );

                hadoopManager.contentRoot.getUI().addWindow( alert.getAlert() );
            }
        };
    }


    protected Button.ClickListener slaveNodeExcludeIncludeButtonListener( final Item row ) {
        final Agent agent = hadoopManager.getAgentByRow( row );
        final HorizontalLayout availableOperationsLayout = hadoopManager.getAvailableOperationsLayout( row );
        final Button checkButton = hadoopManager.getCheckButton( availableOperationsLayout );
        final Button excludeIncludeNodeButton = hadoopManager.getExcludeIncludeButton( availableOperationsLayout );

//
//        if ( agent == null )
//            return new Button.ClickListener() {
//                @Override
//                public void buttonClick( final Button.ClickEvent event ) {
//
//                }
//            };

        return  new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {

                if ( excludeIncludeNodeButton.getCaption().toString().equals( hadoopManager.INCLUDE_BUTTON_CAPTION ) ) {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to include the %s node?", agent.getHostname() ), "Yes",
                            "No" );
                    alert.getOk().addClickListener( new Button.ClickListener() {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent ) {
                            excludeIncludeNodeButton.setEnabled( false );
                            UUID trackID = HadoopUI.getHadoopManager().includeNode( hadoopManager.hadoopCluster, agent );
                            ProgressWindow window =
                                    new ProgressWindow( HadoopUI.getExecutor(), HadoopUI.getTracker(), trackID,
                                            HadoopClusterConfig.PRODUCT_KEY );
                            window.getWindow().addCloseListener( new Window.CloseListener() {
                                @Override
                                public void windowClose( Window.CloseEvent closeEvent ) {
                                    checkButton.click();
                                    excludeIncludeNodeButton.setEnabled( true );
                                }
                            } );
                            hadoopManager.contentRoot.getUI().addWindow( window.getWindow() );
                        }
                    } );

                    hadoopManager.contentRoot.getUI().addWindow( alert.getAlert() );
                }
                else {
                    ConfirmationDialog alert = new ConfirmationDialog(
                            String.format( "Do you want to exclude the %s node?", agent.getHostname() ), "Yes",
                            "No" );
                    alert.getOk().addClickListener( new Button.ClickListener() {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent ) {
                            excludeIncludeNodeButton.setEnabled( false );
                            UUID trackID = HadoopUI.getHadoopManager().excludeNode( hadoopManager.hadoopCluster, agent );
                            ProgressWindow window =
                                    new ProgressWindow( HadoopUI.getExecutor(), HadoopUI.getTracker(), trackID,
                                            HadoopClusterConfig.PRODUCT_KEY );
                            window.getWindow().addCloseListener( new Window.CloseListener() {
                                @Override
                                public void windowClose( Window.CloseEvent closeEvent ) {
                                    hadoopManager.checkAllButton.click();
                                    excludeIncludeNodeButton.setEnabled( true );
                                }
                            } );
                            hadoopManager.contentRoot.getUI().addWindow( window.getWindow() );
                        }
                    } );

                    hadoopManager.contentRoot.getUI().addWindow( alert.getAlert() );
                }
            }
        } ;
    }


    protected Button.ClickListener secondaryNameNodeCheckButtonListener( final Item row ) {
        final Agent agent = hadoopManager.getAgentByRow( row );
        final HorizontalLayout availableOperationsLayout = Manager.getAvailableOperationsLayout( row );
        final HorizontalLayout statusGroupLayout = hadoopManager.getStatusGroupByRow( row );
        final Button checkButton = Manager.getCheckButton( availableOperationsLayout );
        final Label statusDatanode = hadoopManager.getStatusDatanodeLabel( statusGroupLayout );


        //        if ( agent == null )
        //            return new Button.ClickListener() {
        //                @Override
        //                public void buttonClick( final Button.ClickEvent event ) {
        //
        //                }
        //            };

        return new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                hadoopManager.enableProgressBar();
                checkButton.setEnabled( false );
                HadoopUI.getExecutor().execute(
                        new CheckTask( NodeType.SECONDARY_NAMENODE, hadoopManager.hadoopCluster, new CompleteEvent() {

                            public void onComplete( NodeState state ) {
                                if ( state == NodeState.RUNNING ) {
                                    statusDatanode.setValue( "SecondaryNameNode Running" );
                                }
                                else if ( state == NodeState.STOPPED ) {
                                    statusDatanode.setValue( "SecondaryNameNode Stopped" );
                                }
                                else {
                                    statusDatanode.setValue( "SecondaryNameNode Not Connected" );
                                }

                                checkButton.setEnabled( true );
                                hadoopManager.disableProgressBar();
                                if ( hadoopManager.processCount == 0 ) {
                                    hadoopManager.checkAllButton.setEnabled( true );
                                }
                            }
                        }, null, agent ) );
            }
        };
    }


    protected Button.ClickListener secondaryNameNodeURLButtonListener( final Agent agent ) {
        return new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent event ) {
                hadoopManager.contentRoot.getUI().getPage().open( "http://" + agent.getListIP().get( 0 ) + ":50075",
                        "SecondaryNameNode", false );
            }
        };
    }


    protected Button.ClickListener jobTrackerStartStopButtonListener( final Item row ) {
        final Agent agent = hadoopManager.getAgentByRow( row );
        final HorizontalLayout availableOperationsLayout = hadoopManager.getAvailableOperationsLayout( row );
        final Button startStopButton = hadoopManager.getStartButton( availableOperationsLayout );

//        if ( agent == null )
//            return new Button.ClickListener() {
//                @Override
//                public void buttonClick( final Button.ClickEvent event ) {
//
//                }
//            };

        return new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                startStopButton.setEnabled( false );
                hadoopManager.enableProgressBar();
                boolean isRunning = startStopButton.getCaption().equals( hadoopManager.STOP_JOBTRACKER_BUTTON_CAPTION );
                if ( !isRunning ) {
                    startStopButton.setEnabled( false );
                    HadoopUI.getExecutor().execute(
                            new StartTask( NodeType.JOBTRACKER, hadoopManager.hadoopCluster, new CompleteEvent() {

                                public void onComplete( NodeState state ) {
                                    try {
                                        Thread.sleep( 1000 );
                                    }
                                    catch ( InterruptedException e ) {
                                        hadoopManager.show( "Exception: " + e );
                                    }
                                    hadoopManager.disableProgressBar();
                                    hadoopManager.checkAllIfNoProcessRunning();
                                    startStopButton.setEnabled( true );
                                }
                            }, null, agent ) );
                }
                else {
                    startStopButton.setEnabled( false );
                    HadoopUI.getExecutor().execute(
                            new StopTask( NodeType.JOBTRACKER, hadoopManager.hadoopCluster, new CompleteEvent() {

                                public void onComplete( NodeState state ) {
                                    hadoopManager.disableProgressBar();
                                    hadoopManager.checkAllIfNoProcessRunning();
                                    startStopButton.setEnabled( true );
                                }
                            }, null, agent ) );
                }
            }
        } ;
    }


    protected Button.ClickListener jobTrackerCheckButtonListener( final Item row ) {

        final Agent agent = hadoopManager.getAgentByRow( row );
        final HorizontalLayout availableOperationsLayout = hadoopManager.getAvailableOperationsLayout( row );
        final HorizontalLayout statusGroupLayout = hadoopManager.getStatusGroupByRow( row );
        final Button startStopButton = hadoopManager.getStartStopButton( availableOperationsLayout );
        final Button checkButton = hadoopManager.getCheckButton( availableOperationsLayout );
        final Label statusTaskTracker = hadoopManager.getStatusDatanodeLabel( statusGroupLayout );

        //        if ( agent == null )
        //            return new Button.ClickListener() {
        //                @Override
        //                public void buttonClick( final Button.ClickEvent event ) {
        //
        //                }
        //            };

        return new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                hadoopManager.enableProgressBar();
                startStopButton.setEnabled( false );
                checkButton.setEnabled( false );
                HadoopUI.getExecutor()
                        .execute( new CheckTask( NodeType.JOBTRACKER, hadoopManager.hadoopCluster, new CompleteEvent() {

                            public void onComplete( NodeState state ) {
                                if ( state == NodeState.RUNNING ) {
                                    statusTaskTracker.setValue( "JobTracker Running" );
                                    startStopButton.setCaption( hadoopManager.STOP_JOBTRACKER_BUTTON_CAPTION );
                                    startStopButton.setEnabled( true );
                                }
                                else if ( state == NodeState.STOPPED ) {
                                    statusTaskTracker.setValue( "JobTracker Stopped" );
                                    startStopButton.setCaption( hadoopManager.START_JOBTRACKER_BUTTON_CAPTION );
                                    startStopButton.setEnabled( true );
                                }
                                else {
                                    statusTaskTracker.setValue( "JobTracker Not Connected" );
                                    startStopButton.setCaption( "Not connected" );
                                }

                                checkButton.setEnabled( true );
                                hadoopManager.disableProgressBar();
                                if ( hadoopManager.processCount == 0 ) {
                                    hadoopManager.checkAllButton.setEnabled( true );
                                }
                            }
                        }, null, agent ) );
            }
        };

    }

    protected Button.ClickListener nameNodeCheckButtonListener( final Item row ) {

        final Agent agent = hadoopManager.getAgentByRow( row );
        final HorizontalLayout availableOperationsLayout = hadoopManager.getAvailableOperationsLayout( row );
        final HorizontalLayout statusGroupLayout = hadoopManager.getStatusGroupByRow( row );
        final Button startStopButton = hadoopManager.getStartStopButton( availableOperationsLayout );
        final Button checkButton = hadoopManager.getCheckButton( availableOperationsLayout );
        final Label statusDatanode = hadoopManager.getStatusDatanodeLabel( statusGroupLayout );

        return new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                hadoopManager.enableProgressBar();
                checkButton.setEnabled( false );
                startStopButton.setEnabled( false );
                HadoopUI.getExecutor()
                        .execute( new CheckTask( NodeType.NAMENODE, hadoopManager.hadoopCluster, new CompleteEvent() {

                            public void onComplete( NodeState state ) {
                                if ( state == NodeState.RUNNING ) {
                                    statusDatanode.setValue( "Namenode Running" );
                                    startStopButton.setCaption( hadoopManager.STOP_NAMENODE_BUTTON_CAPTION );
                                    startStopButton.setEnabled( true );
                                }
                                else if ( state == NodeState.STOPPED ) {
                                    statusDatanode.setValue( "Namenode Stopped" );
                                    startStopButton.setCaption( hadoopManager.START_NAMENODE_BUTTON_CAPTION );
                                    startStopButton.setEnabled( true );
                                }
                                else {
                                    statusDatanode.setValue( "Namenode Not Connected" );
                                    startStopButton.setCaption( "Not connected" );
                                }
                                checkButton.setEnabled( true );
                                hadoopManager.disableProgressBar();
                                if ( hadoopManager.processCount == 0 ) {
                                    hadoopManager.checkAllButton.setEnabled( true );
                                }
                            }
                        }, null, agent ) );
            }
        };
    }


    protected Button.ClickListener nameNodeStartStopButtonListener( final Item row ) {

        final Agent agent = hadoopManager.getAgentByRow( row );
        final HorizontalLayout availableOperationsLayout = hadoopManager.getAvailableOperationsLayout( row );
        final HorizontalLayout statusGroupLayout = hadoopManager.getStatusGroupByRow( row );
        final Button startStopButton = hadoopManager.getStartStopButton( availableOperationsLayout );

//        if ( agent == null )
//            return new Button.ClickListener() {
//                @Override
//                public void buttonClick( final Button.ClickEvent event ) {
//
//                }
//            };

        return  new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                startStopButton.setEnabled( false );
                hadoopManager.enableProgressBar();
                boolean isRunning = startStopButton.getCaption().equals( hadoopManager.STOP_NAMENODE_BUTTON_CAPTION );
                if ( !isRunning ) {
                    HadoopUI.getExecutor().execute(

                            new StartTask( NodeType.NAMENODE, hadoopManager.hadoopCluster, new CompleteEvent() {

                                public void onComplete( NodeState state ) {
                                    try {
                                        Thread.sleep( 1000 );
                                    }
                                    catch ( InterruptedException e ) {
                                        hadoopManager.show( "Exception: " + e );
                                    }
                                    hadoopManager.disableProgressBar();
                                    hadoopManager.checkAllIfNoProcessRunning();
                                    startStopButton.setEnabled( true );
                                }
                            }, null, agent ) );
                }
                else {
                    HadoopUI.getExecutor().execute(

                            new StopTask( NodeType.NAMENODE, hadoopManager.hadoopCluster, new CompleteEvent() {

                                public void onComplete( NodeState state ) {

                                    hadoopManager.disableProgressBar();
                                    hadoopManager.checkAllIfNoProcessRunning();
                                    startStopButton.setEnabled( true );
                                }
                            }, null, agent ) );
                }
            }
        } ;
    }


    protected Button.ClickListener nameNodeURLButtonListener( final Agent agent ) {
        return new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent event ) {
                hadoopManager.contentRoot.getUI().getPage().open( "http://" + agent.getListIP().get( 0 ) + ":50070",
                        "Namenode", false );
            }
        };
    }



    protected Button.ClickListener slaveNodeCheckButtonListener( final Item row ) {

        final Agent agent = hadoopManager.getAgentByRow( row );
        final HorizontalLayout availableOperationsLayout = hadoopManager.getAvailableOperationsLayout( row );
        final HorizontalLayout statusGroupLayout = hadoopManager.getStatusGroupByRow( row );
        final Button excludeIncludeNodeButton = hadoopManager.getExcludeIncludeButton( availableOperationsLayout );
        final Button destroyButton = hadoopManager.getDestroyButton( availableOperationsLayout );
        final Label statusDatanode = hadoopManager.getStatusDatanodeLabel( statusGroupLayout );
        final Label statusTaskTracker = hadoopManager.getStatusTaskTrackerLabel( statusGroupLayout );


        return new Button.ClickListener() {
            @Override
            public void buttonClick( final Button.ClickEvent clickEvent ) {

                clickEvent.getButton().setEnabled( false );
                excludeIncludeNodeButton.setEnabled( false );
                destroyButton.setEnabled( false );
                if ( HadoopUI.getHadoopManager().getCluster( hadoopManager.hadoopCluster.getClusterName() ).isDataNode( agent ) ) {
                    hadoopManager.enableProgressBar();
                    HadoopUI.getExecutor()
                            .execute( new CheckTask( NodeType.DATANODE, hadoopManager.hadoopCluster, new CompleteEvent() {


                                public void onComplete( NodeState state ) {
                                    if ( state == NodeState.RUNNING ) {
                                        statusDatanode.setValue( "Datanode Running" );
                                        excludeIncludeNodeButton.setEnabled( true );
                                    }
                                    else if ( state == NodeState.STOPPED ) {
                                        statusDatanode.setValue( "Datanode Stopped" );
                                        excludeIncludeNodeButton.setEnabled( true );
                                    }
                                    else {
                                        statusDatanode.setValue( "Not connected" );
                                        excludeIncludeNodeButton.setCaption( "Not connected" );
                                        excludeIncludeNodeButton.setEnabled( false );
                                    }

                                    if ( hadoopManager.checkAllButton.isEnabled() ) {
                                        checkDecommissioningStatus( row, clickEvent.getButton() );
                                    }
                                    else {
                                        executeSlaveNodeCheckButtonFinishCommands( row, clickEvent.getButton() );
                                    }
                                }
                            }, null, agent ) );
                }
                if ( HadoopUI.getHadoopManager().getCluster( hadoopManager.hadoopCluster.getClusterName() ).isTaskTracker( agent ) ) {
                    hadoopManager.enableProgressBar();
                    HadoopUI.getExecutor()
                            .execute( new CheckTask( NodeType.TASKTRACKER, hadoopManager.hadoopCluster, new CompleteEvent() {

                                public void onComplete( NodeState state ) {
                                    if ( state == NodeState.RUNNING ) {
                                        statusTaskTracker.setValue( "Tasktracker Running" );
                                        excludeIncludeNodeButton.setEnabled( true );
                                    }
                                    else if ( state == NodeState.STOPPED ) {
                                        statusTaskTracker.setValue( "Tasktracker Stopped" );
                                        excludeIncludeNodeButton.setEnabled( true );
                                    }
                                    else {
                                        statusTaskTracker.setValue( "Not connected" );
                                        excludeIncludeNodeButton.setCaption( "Not connected" );
                                        excludeIncludeNodeButton.setEnabled( false );
                                    }
                                    clickEvent.getButton().setEnabled( true );
                                    hadoopManager.disableProgressBar();
                                    if ( hadoopManager.processCount == 0 ) {
                                        hadoopManager.checkAllButton.setEnabled( true );
                                        clickEvent.getButton().setEnabled( true );
                                    }
                                }
                            }, null, agent ) );
                }
            }
        };
    }




    public void refreshClusterList() {
        if ( HadoopUI.getHadoopManager() == null )
            return;
        List<HadoopClusterConfig> hadoopClusterList = HadoopUI.getHadoopManager().getClusters();
        HadoopClusterConfig clusterInfo = ( HadoopClusterConfig ) hadoopManager.clusterList.getValue();
        //        if ( clusterInfo == null )
        //            return;
        hadoopManager.clusterList.removeAllItems();
        if ( hadoopClusterList != null && hadoopClusterList.size() > 0 ) {
            for ( HadoopClusterConfig hadoopCluster : hadoopClusterList ) {
                hadoopManager.clusterList.addItem( hadoopCluster );
                hadoopManager.clusterList.setItemCaption( hadoopCluster, hadoopCluster.getClusterName() );
            }
            if ( clusterInfo != null ) {
                for ( HadoopClusterConfig hadoopCluster : hadoopClusterList ) {
                    if ( hadoopCluster.getClusterName().equals( clusterInfo.getClusterName() ) ) {
                        hadoopManager.clusterList.setValue( hadoopCluster );
                        return;
                    }
                }
            }
            else {
                hadoopManager.clusterList.setValue( hadoopClusterList.iterator().next() );
            }
        }
    }



    private void executeSlaveNodeCheckButtonFinishCommands( Item row, Button checkButton ) {
        final Agent agent = hadoopManager.getAgentByRow( row );
        final HorizontalLayout availableOperationsLayout = hadoopManager.getAvailableOperationsLayout( row );
        final HorizontalLayout statusGroupLayout = hadoopManager.getStatusGroupByRow( row );
        final Button destroyButton = hadoopManager.getDestroyButton( availableOperationsLayout );
        final Label statusDecommission = hadoopManager.getStatusDecommissionLabel( statusGroupLayout );
        if ( agent != null ) {
            statusDecommission.setValue( hadoopManager.DECOMMISSION_STATUS_CAPTION + hadoopManager
                            .getDecommissionStatus( hadoopManager.decommissionStatus, agent )
                                       );
            checkButton.setEnabled( true );
            destroyButton.setEnabled( true );
            hadoopManager.disableProgressBar();
            if ( hadoopManager.processCount == 0 ) {
                hadoopManager.checkAllButton.setEnabled( true );
                checkButton.setEnabled( true );
            }
        }
        else {
            statusDecommission.setValue( hadoopManager.DECOMMISSION_STATUS_CAPTION + NodeState.UNKNOWN );
            checkButton.setEnabled( true );
            destroyButton.setEnabled( true );
            hadoopManager.disableProgressBar();
            if ( hadoopManager.processCount == 0 ) {
                hadoopManager.checkAllButton.setEnabled( true );
                checkButton.setEnabled( true );
            }
        }
    }




    private void checkDecommissioningStatus( final Item row, final Button checkButton) {
        HadoopUI.getExecutor()
                .execute( new CheckDecommissionStatusTask( hadoopManager.hadoopCluster,
                        new org.safehaus.subutai.plugin.hadoop.ui.manager.components.CompleteEvent() {

                            public void onComplete( String operationLog ) {
                                hadoopManager.decommissionStatus = operationLog;
                                executeSlaveNodeCheckButtonFinishCommands( row, checkButton );
                            }
                        }, null
                ) );

    }


}
