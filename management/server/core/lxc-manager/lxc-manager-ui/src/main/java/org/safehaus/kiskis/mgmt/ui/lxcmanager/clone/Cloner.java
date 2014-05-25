package org.safehaus.kiskis.mgmt.ui.lxcmanager.clone;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcManager;
import org.safehaus.kiskis.mgmt.server.ui.MgmtAgentManager;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.ui.lxcmanager.LxcUI;

import com.google.common.base.Strings;
import com.vaadin.data.Item;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;


@SuppressWarnings("serial")
public class Cloner extends VerticalLayout {

    private final MgmtAgentManager agentTree;
    private final Button cloneBtn;
    private final TextField textFieldLxcName;
    private final Slider slider;
    private final Label indicator;
    private final TreeTable lxcTable;
    private final LxcManager lxcManager;
    private final String physicalHostLabel = "Physical Host";
    private final String statusLabel = "Status";
    private final String okIconSource = "icons/16/ok.png";
    private final String errorIconSource = "icons/16/cancel.png";
    private final String loadIconSource = "../base/common/img/loading-indicator.gif";
    private final String hostValidatorRegex =
            "^(?=.{1,255}$)[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,61}[0-9A-Za-z])?(?:\\.[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0," +
                    "61}[0-9A-Za-z])?)*\\.?$";


    public Cloner( final LxcManager lxcManager, MgmtAgentManager agentTree ) {
        setSpacing( true );
        setMargin( true );

        this.agentTree = agentTree;

        this.lxcManager = lxcManager;

        textFieldLxcName = new TextField();
        slider = new Slider();
        slider.setMin( 1 );
        slider.setMax( 20 );
        slider.setWidth( 150, Sizeable.UNITS_PIXELS );
        slider.setImmediate( true );
        cloneBtn = new Button( "Clone" );
        cloneBtn.addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( Button.ClickEvent event ) {
                startCloneTask();
            }
        } );

        Button clearBtn = new Button( "Clear" );
        clearBtn.addListener( new Button.ClickListener() {

            @Override
            public void buttonClick( Button.ClickEvent event ) {
                //clear completed
                for ( Object rowId : lxcTable.getItemIds() ) {
                    Item row = lxcTable.getItem( rowId );
                    if ( row != null ) {
                        Embedded statusIcon = ( Embedded ) ( row.getItemProperty( statusLabel ).getValue() );
                        if ( statusIcon != null && (
                                okIconSource.equals( ( ( ThemeResource ) statusIcon.getSource() ).getResourceId() )
                                        || errorIconSource
                                        .equals( ( ( ThemeResource ) statusIcon.getSource() ).getResourceId() ) ) ) {
                            lxcTable.removeItem( rowId );
                        }
                    }
                }
                //clear empty parents
                for ( Object rowId : lxcTable.getItemIds() ) {
                    Item row = lxcTable.getItem( rowId );
                    if ( row != null && row.getItemProperty( physicalHostLabel ).getValue() != null && (
                            lxcTable.getChildren( rowId ) == null || lxcTable.getChildren( rowId ).isEmpty() ) ) {
                        lxcTable.removeItem( rowId );
                    }
                }
            }
        } );

        indicator = new Label();
        indicator.setIcon( new ThemeResource( "icons/indicator.gif" ) );
        indicator.setContentMode( Label.CONTENT_XHTML );
        indicator.setHeight( 11, Sizeable.UNITS_PIXELS );
        indicator.setWidth( 50, Sizeable.UNITS_PIXELS );
        indicator.setVisible( false );

        GridLayout topContent = new GridLayout( 7, 1 );
        topContent.setSpacing( true );

        topContent.addComponent( new Label( "Product name" ) );
        topContent.addComponent( textFieldLxcName );
        topContent.addComponent( new Label( "Lxc count" ) );
        topContent.addComponent( slider );
        topContent.addComponent( cloneBtn );
        topContent.addComponent( clearBtn );
        topContent.addComponent( indicator );
        topContent.setComponentAlignment( indicator, Alignment.MIDDLE_CENTER );
        addComponent( topContent );

        lxcTable = createLxcTable( "Lxc containers", 500 );
        addComponent( lxcTable );
    }


    private TreeTable createLxcTable( String caption, int size ) {
        TreeTable table = new TreeTable( caption );
        table.addContainerProperty( physicalHostLabel, String.class, null );
        table.addContainerProperty( "Lxc Host", String.class, null );
        table.addContainerProperty( statusLabel, Embedded.class, null );
        table.setWidth( 100, Sizeable.UNITS_PERCENTAGE );
        table.setHeight( size, Sizeable.UNITS_PIXELS );
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );
        return table;
    }


    private void startCloneTask() {
        Set<Agent> physicalAgents = Util.filterPhysicalAgents( agentTree.getSelectedAgents() );
        final String productName = textFieldLxcName.getValue().toString().trim();

        if ( !Strings.isNullOrEmpty( productName ) && !productName.matches( hostValidatorRegex ) ) {
            show( "Please, use only letters, digits, dots and hyphens in product name" );
        }
        else if ( physicalAgents.isEmpty() ) {
            indicator.setVisible( true );
            final double count = ( Double ) slider.getValue();
            LxcUI.getExecutor().execute( new Runnable() {
                public void run() {
                    Map<Agent, Integer> bestServers = lxcManager.getPhysicalServersWithLxcSlots();
                    if ( bestServers.isEmpty() ) {
                        show( "No servers available to accommodate new lxc containers" );
                        indicator.setVisible( false );
                    }
                    else {
                        int numOfLxcSlots = 0;
                        for ( Map.Entry<Agent, Integer> srv : bestServers.entrySet() ) {
                            numOfLxcSlots += srv.getValue();
                        }

                        if ( numOfLxcSlots < count ) {
                            show( String.format( "Only %s lxc containers can be created", numOfLxcSlots ) );
                            indicator.setVisible( false );
                        }
                        else {

                            Map<Agent, List<String>> agentFamilies = new HashMap<>();
                            int numOfLxcsToClone = ( int ) count;
                            final AtomicInteger countProcessed = new AtomicInteger( numOfLxcsToClone );

                            for ( int i = 1; i <= numOfLxcsToClone; i++ ) {
                                Map<Agent, Integer> sortedBestServers = Util.sortMapByValueDesc( bestServers );
                                final Map.Entry<Agent, Integer> entry = sortedBestServers.entrySet().iterator().next();
                                bestServers.put( entry.getKey(), entry.getValue() - 1 );
                                List<String> lxcHostNames = agentFamilies.get( entry.getKey() );
                                if ( lxcHostNames == null ) {
                                    lxcHostNames = new ArrayList<>();
                                    agentFamilies.put( entry.getKey(), lxcHostNames );
                                }
                                final StringBuilder lxcHost = new StringBuilder();
                                if ( !Strings.isNullOrEmpty( productName ) ) {
                                    lxcHost.append( productName );
                                    lxcHost.append( lxcHostNames.size() + 1 );
                                    lxcHost.append( "-" );
                                }
                                lxcHost.append( Util.generateTimeBasedUUID().toString() );
                                lxcHostNames.add( lxcHost.toString() );

                                //start clone task
                                LxcUI.getExecutor().execute( new Runnable() {
                                    public void run() {
                                        boolean result =
                                                lxcManager.cloneLxcOnHost( entry.getKey(), lxcHost.toString() );
                                        Item row = lxcTable.getItem( lxcHost.toString() );
                                        if ( row != null ) {
                                            if ( result ) {
                                                row.getItemProperty( "Status" )
                                                   .setValue( new Embedded( "", new ThemeResource( okIconSource ) ) );
                                            }
                                            else {
                                                row.getItemProperty( "Status" ).setValue(
                                                        new Embedded( "", new ThemeResource( errorIconSource ) ) );
                                            }
                                        }
                                        if ( countProcessed.decrementAndGet() == 0 ) {
                                            indicator.setVisible( false );
                                        }
                                    }
                                } );
                            }

                            populateLxcTable( agentFamilies );
                        }
                    }
                }
            } );
        }
        else {

            Map<Agent, List<String>> agentFamilies = new HashMap<>();
            double count = ( Double ) slider.getValue();
            for ( Agent physAgent : physicalAgents ) {
                List<String> lxcHostNames = new ArrayList<>();
                for ( int i = 1; i <= count; i++ ) {
                    StringBuilder lxcHost = new StringBuilder();
                    if ( !Strings.isNullOrEmpty( productName ) ) {
                        lxcHost.append( productName ).append( i ).append( "-" );
                    }
                    lxcHost.append( Util.generateTimeBasedUUID().toString() );
                    lxcHostNames.add( lxcHost.toString() );
                }
                agentFamilies.put( physAgent, lxcHostNames );
            }

            populateLxcTable( agentFamilies );
            indicator.setVisible( true );
            final AtomicInteger countProcessed = new AtomicInteger( ( int ) ( count * physicalAgents.size() ) );
            for ( final Map.Entry<Agent, List<String>> agg : agentFamilies.entrySet() ) {
                for ( final String lxcHostname : agg.getValue() ) {
                    LxcUI.getExecutor().execute( new Runnable() {
                        public void run() {
                            boolean result = lxcManager.cloneLxcOnHost( agg.getKey(), lxcHostname );
                            Item row = lxcTable.getItem( lxcHostname );
                            if ( row != null ) {
                                if ( result ) {
                                    row.getItemProperty( "Status" )
                                       .setValue( new Embedded( "", new ThemeResource( okIconSource ) ) );
                                }
                                else {
                                    row.getItemProperty( "Status" )
                                       .setValue( new Embedded( "", new ThemeResource( errorIconSource ) ) );
                                }
                            }
                            if ( countProcessed.decrementAndGet() == 0 ) {
                                indicator.setVisible( false );
                            }
                        }
                    } );
                }
            }
        }
    }


    private void populateLxcTable( Map<Agent, List<String>> agents ) {

        for ( final Agent agent : agents.keySet() ) {
            if ( lxcTable.getItem( agent.getHostname() ) == null ) {
                lxcTable.addItem( new Object[] { agent.getHostname(), null, null }, agent.getHostname() );
            }
            lxcTable.setCollapsed( agent.getHostname(), false );
            for ( String lxc : agents.get( agent ) ) {
                Embedded progressIcon = new Embedded( "", new ThemeResource( loadIconSource ) );

                lxcTable.addItem( new Object[] {
                                null, lxc, progressIcon
                        }, lxc
                                );

                lxcTable.setParent( lxc, agent.getHostname() );
                lxcTable.setChildrenAllowed( lxc, false );
            }
        }
    }


    private void show( String msg ) {
        getWindow().showNotification( msg );
    }
}
