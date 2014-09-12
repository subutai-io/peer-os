//package org.safehaus.subutai.plugin.hadoop.ui.manager;
//
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//import org.safehaus.subutai.common.protocol.Agent;
//import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
//import org.safehaus.subutai.plugin.hadoop.ui.HadoopUI;
//import org.safehaus.subutai.plugin.hadoop.ui.manager.components.JobTracker;
//import org.safehaus.subutai.plugin.hadoop.ui.manager.components.NameNode;
//import org.safehaus.subutai.plugin.hadoop.ui.manager.components.SecondaryNameNode;
//import org.safehaus.subutai.plugin.hadoop.ui.manager.components.SlaveNode;
//import org.safehaus.subutai.server.ui.component.ConfirmationDialog;
//import org.safehaus.subutai.server.ui.component.ProgressWindow;
//import org.safehaus.subutai.server.ui.component.QuestionDialog;
//
//import com.vaadin.data.Item;
//import com.vaadin.event.Action;
//import com.vaadin.ui.Button;
//import com.vaadin.ui.Embedded;
//import com.vaadin.ui.Label;
//import com.vaadin.ui.TreeTable;
//import com.vaadin.ui.Window;
//
//
///**
// * Created by daralbaev on 12.04.14.
// */
//public class HadoopTable extends TreeTable {
//    public static final String ALL_CLUSTERS_ROW = "All clusters";
//    public static final String CLUSTERS_PROPERTY = "Clusters";
//    public static final String GENERAL_INFORMATION_PROPERTY = "General Information";
////    public static final String DOMAIN_NAME_PROPERTY = "Domain Name";
////    public static final String NAMENODE_PROPERTY = "NameNode/DataNodes";
////    public static final String SECONDARY_NAMENODE_PROPERTY = "Secondary NameNode";
////    public static final String JOBTRACKER_PROPERTY = "JobTracker/TaskTrackers";
////    public static final String REPLICATION_PROPERTY = "Replication Factor";
//    private static final Action VIEW_CLUSTER_INFO_ACTION = new Action( "View cluster Info" );
//    private static final Action START_NAMENODE_ACTION = new Action( "Start Namenode" );
//    private static final Action STOP_NAMENODE_ACTION = new Action( "Stop Namenode" );
//    private static final Action START_JOBTRACKER_ACTION = new Action( "Start JobTracker" );
//       private static final Action STOP_JOBTRACKER_ACTION = new Action( "Stop JobTracker" );
//    private static final Action UNINSTALL_ITEM_ACTION = new Action( "Uninstall cluster" );
//    private static final Action ADD_ITEM_ACTION = new Action( "Add new node" );
//    private static final Action EXCLUDE_ITEM_ACTION = new Action( "Exclude node" );
//    private static final Action INCLUDE_ITEM_ACTION = new Action( "Include node" );
//    private Embedded indicator;
//
//
//    public HadoopTable( String caption, final Embedded indicator ) {
//        this.indicator = indicator;
//        setCaption( caption );
//
//        this.setSizeFull();
//
//        this.setPageLength( 10 );
//        this.setSelectable( true );
//        this.setImmediate( true );
//
//        addContainerProperty( CLUSTERS_PROPERTY, String.class, null );
//        addContainerProperty( GENERAL_INFORMATION_PROPERTY, Label.class, null );
////        addContainerProperty( DOMAIN_NAME_PROPERTY, String.class, null );
////        addContainerProperty( NAMENODE_PROPERTY, ClusterNode.class, null );
////        addContainerProperty( SECONDARY_NAMENODE_PROPERTY, ClusterNode.class, null );
////        addContainerProperty( JOBTRACKER_PROPERTY, ClusterNode.class, null );
////        addContainerProperty( REPLICATION_PROPERTY, Integer.class, null );
//
//        addActionHandler( new Action.Handler() {
//
//            public Action[] getActions( Object target, Object sender ) {
//
//                if ( target != null ) {
//                    Item row = getItem( target );
//                    List<Action> actionList = new ArrayList<Action>();
//
//                    // If selected row is the cluster's parent item
//                    if ( areChildrenAllowed( target ) &&
//                         ! row.getItemProperty( CLUSTERS_PROPERTY ).getValue().toString().equals( ALL_CLUSTERS_ROW ) ) {
//                        HadoopClusterConfig cluster = HadoopUI.getHadoopManager().getCluster(
//                                row.getItemProperty( CLUSTERS_PROPERTY ).getValue().toString() );
//
//                        UUID trackID = HadoopUI.getHadoopManager().statusNameNode( cluster );
////                        if (  )
//                        actionList.add( START_NAMENODE_ACTION );
//                        actionList.add( STOP_NAMENODE_ACTION );
//
//                        actionList.add( START_JOBTRACKER_ACTION );
//                        actionList.add( STOP_JOBTRACKER_ACTION );
//
//                        actionList.add( ADD_ITEM_ACTION );
//                        actionList.add( VIEW_CLUSTER_INFO_ACTION );
//                        actionList.add( UNINSTALL_ITEM_ACTION );
//                    }
//                    // If selected row is a cluster's child item
//                    else if ( !areChildrenAllowed( target ) ) {
//                        actionList.add( INCLUDE_ITEM_ACTION );
//                        actionList.add( EXCLUDE_ITEM_ACTION );
//                    }
//                    return actionList.toArray(new Action[actionList.size()]);
//                }
//
//                return null;
//            }
//
//
//            public void handleAction( Action action, Object sender, Object target ) {
//                if ( action == UNINSTALL_ITEM_ACTION ) {
//                    final Item row = getItem( target );
//
//                    indicator.setVisible( true );
//
//                    ConfirmationDialog confirmationDialog = new ConfirmationDialog( String.format(
//                            "Do you want destroy %s cluster?",
//                            ( String ) row.getItemProperty( CLUSTERS_PROPERTY ).getValue() ), "Yes", "No"  );
//                    confirmationDialog.getOk().addClickListener( new Button.ClickListener() {
//                        @Override
//                        public void buttonClick( final Button.ClickEvent clickEvent ) {
//
//                            UUID trackID = HadoopUI.getHadoopManager().uninstallCluster(
//                                    ( String ) row.getItemProperty( CLUSTERS_PROPERTY ).getValue() );
//                            final ProgressWindow window =
//                                    new ProgressWindow( HadoopUI.getExecutor(), HadoopUI.getTracker(), trackID,
//                                            HadoopClusterConfig.PRODUCT_KEY );
//
//                            window.getWindow().addCloseListener( new Window.CloseListener() {
//                                @Override
//                                public void windowClose( Window.CloseEvent closeEvent ) {
//                                    refreshDataSource();
//                                }
//                            } );
//
//                            getUI().addWindow( window.getWindow() );
//                        }
//                    } );
//
//                    confirmationDialog.getCancel().addClickListener( new Button.ClickListener() {
//                        @Override
//                        public void buttonClick( final Button.ClickEvent clickEvent ) {
//                            indicator.setVisible( false );
//                        }
//                    } );
//
//                    getUI().addWindow( confirmationDialog.getAlert() );
//                }
//                else if ( action == ADD_ITEM_ACTION ) {
//                    final Item row = getItem( target );
//
//                    indicator.setVisible( true );
//
//                    final QuestionDialog questionDialog = new QuestionDialog<Integer>( ADD_ITEM_ACTION ,
//                            "How many nodes do you want to add?",
//                            Integer.class, "Next","Cancel");
//                    questionDialog.getOk().addClickListener( new Button.ClickListener() {
//                        @Override
//                        public void buttonClick( final Button.ClickEvent clickEvent ) {
//
//                            ConfirmationDialog confirmationDialog = new ConfirmationDialog( String.format(
//                                    "Do you want to add " + questionDialog.getInputField() + " node to %s cluster?",
//                                    ( String ) row.getItemProperty( CLUSTERS_PROPERTY ).getValue() ), "Yes", "No" );
//
//                            confirmationDialog.getOk().addClickListener( new Button.ClickListener() {
//                                @Override
//                                public void buttonClick( final Button.ClickEvent clickEvent ) {
//                                    UUID trackID = HadoopUI.getHadoopManager().addNode(
//                                            ( String ) row.getItemProperty( CLUSTERS_PROPERTY ).getValue(),
//                                            (Integer) questionDialog.getConvertedValue() );
//                                    final ProgressWindow window = new ProgressWindow( HadoopUI.getExecutor(),
//                                            HadoopUI.getTracker(), trackID,
//                                            HadoopClusterConfig.PRODUCT_KEY );
//
//                                    window.getWindow().addCloseListener( new Window.CloseListener() {
//                                        @Override
//                                        public void windowClose( Window.CloseEvent closeEvent ) {
//                                            refreshDataSource();
//                                        }
//                                    } );
//                                    getUI().addWindow( window.getWindow() );
//                                }
//                            } );
//
//                            confirmationDialog.getCancel().addClickListener( new Button.ClickListener() {
//                                @Override
//                                public void buttonClick( final Button.ClickEvent clickEvent ) {
//                                    indicator.setVisible( false );
//                                }
//                            } );
//                            if ( questionDialog.isConvertible( questionDialog.getInputField().getValue() ) )
//                                getUI().addWindow( confirmationDialog.getAlert() );
//                        }
//                    } );
//
//                    questionDialog.getCancel().addClickListener( new Button.ClickListener() {
//                        @Override
//                        public void buttonClick( final Button.ClickEvent clickEvent ) {
//                            indicator.setVisible( false );
//                        }
//                    });
//
//                    getUI().addWindow( questionDialog.getAlert() );
//                }
//                else if ( action == EXCLUDE_ITEM_ACTION ) {
//                    Item row = getItem( target );
//
////                    SlaveNode dataNode = ( SlaveNode ) row.getItemProperty( NAMENODE_PROPERTY ).getValue();
////                    SlaveNode taskTracker = ( SlaveNode ) row.getItemProperty( JOBTRACKER_PROPERTY ).getValue();
////
////                    indicator.setVisible( true );
////
////                    HadoopUI.getHadoopManager().blockDataNode( dataNode.getCluster(), dataNode.getAgent() );
////                    UUID trackID = HadoopUI.getHadoopManager()
////                                           .blockTaskTracker( taskTracker.getCluster(), taskTracker.getAgent() );
////                    HadoopUI.getExecutor().execute( new WaitTask( trackID, new CompleteEvent() {
////
////                        public void onComplete( NodeState state ) {
////                            refreshDataSource();
////                        }
////                    } ) );
//                }
//                else if ( action == INCLUDE_ITEM_ACTION ) {
//                    Item row = getItem( target );
////                    String clusterName = (String) row.getItemProperty( CLUSTERS_PROPERTY ).getValue();
////                    SlaveNode dataNode = ( SlaveNode ) row.getItemProperty( NAMENODE_PROPERTY ).getValue();
////                    SlaveNode taskTracker = ( SlaveNode ) row.getItemProperty( JOBTRACKER_PROPERTY ).getValue();
////
////                    indicator.setVisible( true );
////                    HadoopUI.getHadoopManager().unblockDataNode( dataNode.getCluster(), dataNode.getAgent() );
////
////                    UUID trackID = HadoopUI.getHadoopManager()
////                                           .unblockTaskTracker( taskTracker.getCluster(), taskTracker.getAgent() );
////                    HadoopUI.getExecutor().execute( new WaitTask( trackID, new CompleteEvent() {
////
////                        public void onComplete( NodeState state ) {
////                            refreshDataSource();
////                        }
////                    } ) );
//                }
//            }
//        } );
//
//        refreshDataSource();
//    }
//
//
//    public void refreshDataSource() {
//        indicator.setVisible( true );
//        removeAllItems();
//
//        final Object parentId = addItem( new Object[] { ALL_CLUSTERS_ROW, null }, null );
//        setCollapsed( parentId, false );
//
//        List<HadoopClusterConfig> list = HadoopUI.getHadoopManager().getClusters();
//        for ( HadoopClusterConfig cluster : list ) {
//            NameNode nameNode = new NameNode( cluster );
//            JobTracker jobTracker = new JobTracker( cluster );
//            SecondaryNameNode secondaryNameNode = new SecondaryNameNode( cluster );
//            nameNode.addSlaveNode( secondaryNameNode );
//
//            Object rowId = addItem( new Object[] {
//                    cluster.getClusterName(),
//                    new Label( cluster.getDomainName()
//                            + "\n" +
//                            "Total Node count: "
//                            + "<a href='localhost:50070'>\"\n" + "+ \"Namenode</a>"
//                            + cluster.getAllNodes().size() )
//            }, null );
//
//            for ( Agent agent : cluster.getDataNodes() ) {
//                Object childID = null;
//
//                SlaveNode dataNode = new SlaveNode( cluster, agent, true );
//                SlaveNode taskTracker = new SlaveNode( cluster, agent, false );
//
//                nameNode.addSlaveNode( dataNode );
//                jobTracker.addSlaveNode( taskTracker );
//                Label label = new Label( new StringBuilder( "Node role here" ).toString() );
//                label.setContentMode(Label.CONTENT_XHTML);
//                childID = addItem( new Object[] {
//                        agent.getHostname() + " (" + agent.getListIP().get( 0 ) + ")",
//                        label
//                }, null );
//
//                setParent( childID, rowId );
//                setCollapsed( childID, true );
//                setChildrenAllowed( childID, false );
//            }
//
//            setParent( rowId, parentId );
//            setCollapsed( rowId, false );
//        }
//        indicator.setVisible( false );
//    }
//}
