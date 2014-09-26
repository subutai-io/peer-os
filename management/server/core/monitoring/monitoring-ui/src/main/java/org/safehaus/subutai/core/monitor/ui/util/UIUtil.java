package org.safehaus.subutai.core.monitor.ui.util;


import java.util.Date;

import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.PopupDateField;


public class UIUtil
{

    private UIUtil()
    {
    }


    public static ComboBox getComboBox( String label, String... values )
    {

        ComboBox comboBox = new ComboBox( label );
        comboBox.setInputPrompt( values[0] );

        for ( String value : values )
        {
            comboBox.addItem( value );
        }

        return comboBox;
    }


    public static Button getButton( String name, String width )
    {

        Button button = new Button( name );
        button.setWidth( width );

        return button;
    }


    public static ListSelect addListSelect( AbsoluteLayout parent, String caption, String position, String width,
                                            String height )
    {

        ListSelect list = new ListSelect( caption );
        list.setWidth( width );
        list.setHeight( height );
        list.setNullSelectionAllowed( false );
        list.setImmediate( true );

        parent.addComponent( list, position );

        return list;
    }


    public static PopupDateField addDateField( AbsoluteLayout parent, String label, String position, Date value )
    {

        PopupDateField dateField = new PopupDateField( label, value );
        dateField.setDateFormat( "yyyy-MM-dd HH:mm:ss" );
        dateField.setResolution( Resolution.SECOND );

        parent.addComponent( dateField, position );

        return dateField;
    }


    public static Label addLabel( AbsoluteLayout parent, String text, String position )
    {

        Label label = new Label( text );
        label.setContentMode( ContentMode.HTML );

        parent.addComponent( label, position );

        return label;
    }
}
