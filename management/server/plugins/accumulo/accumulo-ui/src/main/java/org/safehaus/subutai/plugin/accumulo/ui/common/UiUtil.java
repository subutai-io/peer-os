package org.safehaus.subutai.plugin.accumulo.ui.common;


import org.safehaus.subutai.plugin.accumulo.ui.AccumuloUI;
import org.safehaus.subutai.server.ui.component.TerminalWindow;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;


/**
 * Created by dilshat on 4/28/14.
 */
public class UiUtil {

    public static final String MASTER_PREFIX = "Master: ";
    public static final String GC_PREFIX = "GC: ";
    public static final String MONITOR_PREFIX = "Monitor: ";


    public static ComboBox getCombo( String title ) {
        ComboBox combo = new ComboBox( title );
        combo.setImmediate( true );
        combo.setTextInputAllowed( false );
        combo.setRequired( true );
        combo.setNullSelectionAllowed( false );
        return combo;
    }


    public static TwinColSelect getTwinSelect( String title, String captionProperty, String leftTitle,
                                               String rightTitle, int rows ) {
        TwinColSelect twinColSelect = new TwinColSelect( title );
        twinColSelect.setItemCaptionPropertyId( captionProperty );
        twinColSelect.setRows( rows );
        twinColSelect.setMultiSelect( true );
        twinColSelect.setImmediate( true );
        twinColSelect.setLeftColumnCaption( leftTitle );
        twinColSelect.setRightColumnCaption( rightTitle );
        twinColSelect.setWidth( 100, Sizeable.Unit.PERCENTAGE );
        twinColSelect.setRequired( true );
        return twinColSelect;
    }


    public static Table createTableTemplate( String caption, boolean destroyButtonNeeded ) {
        final Table table = new Table( caption );
        table.addContainerProperty( "Host", String.class, null );
        table.addContainerProperty( "Check", Button.class, null );
        if ( destroyButtonNeeded ) {
            table.addContainerProperty( "Destroy", Button.class, null );
        }
        table.addContainerProperty( "Nodes state", Label.class, null );
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
                    lxcHostname = lxcHostname.replace( MASTER_PREFIX, "" ).replace( MONITOR_PREFIX, "" )
                                             .replace( GC_PREFIX, "" );
                    Agent lxcAgent = AccumuloUI.getAgentManager().getAgentByHostname( lxcHostname );
                    if ( lxcAgent != null ) {
                        TerminalWindow terminal =
                                new TerminalWindow( Util.wrapAgentToSet( lxcAgent ), AccumuloUI.getExecutor(),
                                        AccumuloUI.getCommandRunner(), AccumuloUI.getAgentManager() );
                        table.getUI().addWindow( terminal.getWindow() );
                    }
                    else {
                        Notification.show( "Agent is not connected" );
                    }
                }
            }
        } );
        return table;
    }


    public static TextField getTextField( String caption, String prompt, int maxLength ) {
        TextField textField = new TextField( caption );
        textField.setInputPrompt( prompt );
        textField.setMaxLength( maxLength );
        textField.setRequired( true );
        return textField;
    }


    public static void clickAllButtonsInTable( Table table, String buttonCaption ) {
        for ( Object o : table.getItemIds() ) {
            int rowId = ( Integer ) o;
            Item row = table.getItem( rowId );
            Button checkBtn = ( Button ) ( row.getItemProperty( buttonCaption ).getValue() );
            checkBtn.addStyleName( "default" );
            checkBtn.click();
        }
    }
}
