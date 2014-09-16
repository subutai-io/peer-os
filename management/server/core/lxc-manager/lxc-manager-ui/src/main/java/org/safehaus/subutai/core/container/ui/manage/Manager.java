package org.safehaus.subutai.core.container.ui.manage;


import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcManager;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcState;
import org.safehaus.subutai.core.container.ui.common.Buttons;
import org.safehaus.subutai.server.ui.component.ConfirmationDialog;

import com.vaadin.data.Item;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings( "serial" )
public class Manager extends VerticalLayout {

    private final static String physicalHostLabel = "Physical Host";
    private final Label indicator;
    private final Button infoBtn;
    private final Button startAllBtn;
    private final Button stopAllBtn;
    private final Button destroyAllBtn;
    private final TreeTable lxcTable;
    private final LxcManager lxcManager;
    private final AgentManager agentManager;
    private volatile boolean isDestroyAllButtonClicked = false;
    private final Executor executor;


    public Manager( AgentManager agentManager, LxcManager lxcManager, Executor executor ) {

        setSpacing( true );
        setMargin( true );

        this.executor = executor;
        this.agentManager = agentManager;
        this.lxcManager = lxcManager;

        lxcTable = createTableTemplate( "Lxc containers", 500 );

        infoBtn = new Button( Buttons.INFO.getButtonLabel() );
        infoBtn.addStyleName( "default" );
        infoBtn.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                getLxcInfo();
            }
        } );

        stopAllBtn = new Button( Buttons.STOP_ALL.getButtonLabel() );
        stopAllBtn.addStyleName( "default" );
        stopAllBtn.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                for ( Object o : lxcTable.getItemIds() ) {
                    Item row = lxcTable.getItem( o );
                    Button stopBtn = ( Button ) ( row.getItemProperty( Buttons.STOP.getButtonLabel() ).getValue() );
                    if ( stopBtn != null ) {
                        stopBtn.click();
                    }
                }
            }
        } );
        startAllBtn = new Button( Buttons.START_ALL.getButtonLabel() );
        startAllBtn.addStyleName( "default" );
        startAllBtn.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                for ( Object o : lxcTable.getItemIds() ) {
                    Item row = lxcTable.getItem( o );
                    Button startBtn = ( Button ) ( row.getItemProperty( Buttons.START.getButtonLabel() ).getValue() );
                    if ( startBtn != null ) {
                        startBtn.click();
                    }
                }
            }
        } );
        destroyAllBtn = new Button( Buttons.DESTROY_ALL.getButtonLabel() );
        destroyAllBtn.addStyleName( "default" );
        destroyAllBtn.addClickListener( new Button.ClickListener() {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent ) {
                ConfirmationDialog alert =
                        new ConfirmationDialog( "Do you want to destroy all lxc nodes?", "Yes", "No" );
                alert.getOk().addClickListener( new Button.ClickListener() {
                    @Override
                    public void buttonClick( Button.ClickEvent clickEvent ) {
                        isDestroyAllButtonClicked = true;
                        for ( Object o : lxcTable.getItemIds() ) {
                            Item row = lxcTable.getItem( o );
                            Button destroyBtn =
                                    ( Button ) ( row.getItemProperty( Buttons.DESTROY.getButtonLabel() ).getValue() );
                            if ( destroyBtn != null && row.getItemProperty( physicalHostLabel ).getValue() == null ) {
                                destroyBtn.click();
                            }
                        }
                        isDestroyAllButtonClicked = false;
                    }
                } );

                getUI().addWindow( alert.getAlert() );
            }
        } );

        indicator = new Label();
        indicator.setIcon( new ThemeResource( "img/spinner.gif" ) );
        indicator.setContentMode( ContentMode.HTML );
        indicator.setHeight( 11, Unit.PIXELS );
        indicator.setWidth( 50, Unit.PIXELS );
        indicator.setVisible( false );

        GridLayout grid = new GridLayout( 5, 1 );
        grid.setSpacing( true );

        grid.addComponent( infoBtn );
        grid.addComponent( startAllBtn );
        grid.addComponent( stopAllBtn );
        grid.addComponent( destroyAllBtn );
        grid.addComponent( indicator );
        grid.setComponentAlignment( indicator, Alignment.MIDDLE_CENTER );
        addComponent( grid );

        addComponent( lxcTable );
    }


    private TreeTable createTableTemplate( String caption, int size ) {
        TreeTable table = new TreeTable( caption );
        table.addContainerProperty( physicalHostLabel, String.class, null );
        table.addContainerProperty( "Lxc Host", String.class, null );
        table.addContainerProperty( Buttons.START.getButtonLabel(), Button.class, null );
        table.addContainerProperty( Buttons.STOP.getButtonLabel(), Button.class, null );
        table.addContainerProperty( Buttons.DESTROY.getButtonLabel(), Button.class, null );
        table.addContainerProperty( "Status", Embedded.class, null );
        table.setWidth( 100, Unit.PERCENTAGE );
        table.setHeight( size, Unit.PIXELS );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );
        return table;
    }


    public void getLxcInfo() {
        lxcTable.setEnabled( false );
        indicator.setVisible( true );
        executor.execute( new Runnable() {

            public void run() {
                Map<String, EnumMap<LxcState, List<String>>> agentFamilies = lxcManager.getLxcOnPhysicalServers();
                populateTable( agentFamilies );
                clearEmptyParents();
                lxcTable.setEnabled( true );
                indicator.setVisible( false );
            }
        } );
    }


    private void populateTable( Map<String, EnumMap<LxcState, List<String>>> agentFamilies ) {
        lxcTable.removeAllItems();

        for ( Map.Entry<String, EnumMap<LxcState, List<String>>> agentFamily : agentFamilies.entrySet() ) {
            final String parentHostname = agentFamily.getKey();
            final Button startAllChildrenBtn = new Button( Buttons.START.getButtonLabel() );
            startAllChildrenBtn.addStyleName( "default" );
            final Button stopAllChildrenBtn = new Button( Buttons.STOP.getButtonLabel() );
            stopAllChildrenBtn.addStyleName( "default" );
            final Button destroyAllChildrenBtn = new Button( Buttons.DESTROY.getButtonLabel() );
            destroyAllChildrenBtn.addStyleName( "default" );
            final Object parentId = lxcTable.addItem( new Object[] {
                    parentHostname, null, startAllChildrenBtn, stopAllChildrenBtn, destroyAllChildrenBtn, null
            }, parentHostname );
            lxcTable.setCollapsed( parentHostname, false );

            startAllChildrenBtn.addClickListener( new Button.ClickListener() {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent ) {
                    Collection col = lxcTable.getChildren( parentId );
                    if ( col != null ) {
                        for ( Object aCol : col ) {
                            Item row = lxcTable.getItem( aCol );
                            Button startBtn =
                                    ( Button ) ( row.getItemProperty( Buttons.START.getButtonLabel() ).getValue() );
                            startBtn.addStyleName( "default" );
                            if ( startBtn != null ) {
                                startBtn.click();
                            }
                        }
                    }
                }
            } );

            stopAllChildrenBtn.addClickListener( new Button.ClickListener() {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent ) {
                    Collection col = lxcTable.getChildren( parentId );
                    if ( col != null ) {
                        for ( Object aCol : col ) {
                            Item row = lxcTable.getItem( aCol );
                            Button stopBtn =
                                    ( Button ) ( row.getItemProperty( Buttons.STOP.getButtonLabel() ).getValue() );
                            stopBtn.addStyleName( "default" );
                            if ( stopBtn != null ) {
                                stopBtn.click();
                            }
                        }
                    }
                }
            } );

            destroyAllChildrenBtn.addClickListener( new Button.ClickListener() {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent ) {

                    ConfirmationDialog alert =
                            new ConfirmationDialog( "Do you want to destroy all lxc nodes on this physical node?",
                                    "Yes", "No" );
                    alert.getOk().addClickListener( new Button.ClickListener() {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent ) {
                            Collection col = lxcTable.getChildren( parentId );
                            if ( col != null ) {
                                isDestroyAllButtonClicked = true;
                                for ( Object aCol : col ) {
                                    Item row = lxcTable.getItem( aCol );
                                    Button destroyBtn =
                                            ( Button ) ( row.getItemProperty( Buttons.DESTROY.getButtonLabel() )
                                                            .getValue() );
                                    if ( destroyBtn != null ) {
                                        destroyBtn.click();
                                    }
                                }
                                isDestroyAllButtonClicked = false;
                            }
                        }
                    } );

                    getUI().addWindow( alert.getAlert() );
                }
            } );

            for ( Map.Entry<LxcState, List<String>> lxcs : agentFamily.getValue().entrySet() ) {

                for ( final String lxcHostname : lxcs.getValue() ) {
                    final Button startBtn = new Button( Buttons.START.getButtonLabel() );
                    startBtn.addStyleName( "default" );
                    final Button stopBtn = new Button( Buttons.STOP.getButtonLabel() );
                    stopBtn.addStyleName( "default" );
                    final Button destroyBtn = new Button( Buttons.DESTROY.getButtonLabel() );
                    destroyBtn.addStyleName( "default" );
                    final Embedded progressIcon = new Embedded( "", new ThemeResource( "img/spinner.gif" ) );
                    progressIcon.setVisible( false );

                    if ( lxcs.getKey() == LxcState.RUNNING ) {
                        startBtn.setEnabled( false );
                    }
                    else if ( lxcs.getKey() == LxcState.STOPPED ) {
                        stopBtn.setEnabled( false );
                    }
                    final Object rowId = lxcTable.addItem( new Object[] {
                            null, lxcHostname, startBtn, stopBtn, destroyBtn, progressIcon
                    }, lxcHostname );

                    lxcTable.setParent( lxcHostname, parentHostname );
                    lxcTable.setChildrenAllowed( lxcHostname, false );

                    startBtn.addClickListener( new Button.ClickListener() {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent ) {
                            final Agent physicalAgent = agentManager.getAgentByHostname( parentHostname );
                            if ( physicalAgent != null ) {
                                startBtn.setEnabled( false );
                                destroyBtn.setEnabled( false );
                                progressIcon.setVisible( true );
                                executor.execute( new Runnable() {

                                    public void run() {
                                        boolean success = lxcManager.startLxcOnHost( physicalAgent, lxcHostname );
                                        if ( success ) {
                                            stopBtn.setEnabled( true );
                                        }
                                        else {
                                            startBtn.setEnabled( true );
                                        }
                                        destroyBtn.setEnabled( true );
                                        progressIcon.setVisible( false );
                                    }
                                } );
                            }
                        }
                    } );
                    stopBtn.addClickListener( new Button.ClickListener() {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent ) {
                            final Agent physicalAgent = agentManager.getAgentByHostname( parentHostname );
                            if ( physicalAgent != null ) {
                                stopBtn.setEnabled( false );
                                destroyBtn.setEnabled( false );
                                progressIcon.setVisible( true );
                                executor.execute( new Runnable() {

                                    public void run() {
                                        boolean success = lxcManager.stopLxcOnHost( physicalAgent, lxcHostname );
                                        if ( !success ) {
                                            stopBtn.setEnabled( true );
                                        }
                                        else {
                                            startBtn.setEnabled( true );
                                        }
                                        destroyBtn.setEnabled( true );
                                        progressIcon.setVisible( false );
                                    }
                                } );
                            }
                        }
                    } );
                    destroyBtn.addClickListener( new Button.ClickListener() {
                        @Override
                        public void buttonClick( Button.ClickEvent clickEvent ) {
                            if ( !isDestroyAllButtonClicked ) {

                                ConfirmationDialog alert =
                                        new ConfirmationDialog( "Do you want to destroy this lxc node?", "Yes", "No" );
                                alert.getOk().addClickListener( new Button.ClickListener() {
                                    @Override
                                    public void buttonClick( Button.ClickEvent clickEvent ) {
                                        final Agent physicalAgent = agentManager.getAgentByHostname( parentHostname );
                                        if ( physicalAgent != null ) {
                                            startBtn.setEnabled( false );
                                            stopBtn.setEnabled( false );
                                            destroyBtn.setEnabled( false );
                                            progressIcon.setVisible( true );
                                            executor.execute( new Runnable() {

                                                public void run() {
                                                    boolean success =
                                                            lxcManager.destroyLxcOnHost( physicalAgent, lxcHostname );
                                                    if ( !success ) {
                                                        stopBtn.setEnabled( true );
                                                        destroyBtn.setEnabled( true );
                                                        progressIcon.setVisible( false );
                                                    }
                                                    else {
                                                        //remove row
                                                        lxcTable.removeItem( rowId );
                                                        clearEmptyParents();
                                                    }
                                                }
                                            } );
                                        }
                                    }
                                } );

                                getUI().addWindow( alert.getAlert() );
                            }
                            else {

                                final Agent physicalAgent = agentManager.getAgentByHostname( parentHostname );
                                if ( physicalAgent != null ) {
                                    startBtn.setEnabled( false );
                                    stopBtn.setEnabled( false );
                                    destroyBtn.setEnabled( false );
                                    progressIcon.setVisible( true );
                                    executor.execute( new Runnable() {

                                        public void run() {
                                            boolean success = lxcManager.destroyLxcOnHost( physicalAgent, lxcHostname );
                                            if ( !success ) {
                                                stopBtn.setEnabled( true );
                                                destroyBtn.setEnabled( true );
                                                progressIcon.setVisible( false );
                                            }
                                            else {
                                                //remove row
                                                lxcTable.removeItem( rowId );
                                                clearEmptyParents();
                                            }
                                        }
                                    } );
                                }
                            }
                        }
                    } );
                }
            }
        }
    }


    private void clearEmptyParents() {
        //clear empty parents
        for ( Object rowId : lxcTable.getItemIds() ) {
            Item row = lxcTable.getItem( rowId );
            if ( row != null && row.getItemProperty( physicalHostLabel ).getValue() != null && (
                    lxcTable.getChildren( rowId ) == null || lxcTable.getChildren( rowId ).isEmpty() ) ) {
                lxcTable.removeItem( rowId );
            }
        }
    }
}
