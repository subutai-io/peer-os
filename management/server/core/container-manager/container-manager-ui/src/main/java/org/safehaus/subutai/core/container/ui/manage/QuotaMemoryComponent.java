package org.safehaus.subutai.core.container.ui.manage;


import org.safehaus.subutai.core.lxc.quota.api.MemoryUnit;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextField;


/**
 * Created by talas on 10/8/14.
 */
public class QuotaMemoryComponent extends CustomComponent
{
    private TextField memoryTextField = new TextField( "Memory limit" );
    private ComboBox unitComboBox = new ComboBox( "Memory unit", getUnitsEnum() );

    private static final String UNIT_SHORT_NAME = "ShortName";
    private static final String UNIT_LONG_NAME = "LongName";


    public QuotaMemoryComponent()
    {
        setHeight( 100, Unit.PERCENTAGE );
        memoryTextField = new TextField( "Memory limit" );
        unitComboBox = new ComboBox( "Memory unit", getUnitsEnum() );
        Layout layout = new HorizontalLayout();

        unitComboBox.select( MemoryUnit.BYTES.getShortName() );
        memoryTextField.setValue( "0" );
        layout.addComponent( memoryTextField );
        layout.addComponent( unitComboBox );

        setCompositionRoot( layout );
        setSizeFull();
    }


    public QuotaMemoryComponent( String memoryAvailable, MemoryUnit memoryUnit )
    {
        memoryTextField = new TextField( "Memory limit" );
        unitComboBox = new ComboBox( "Memory unit", getUnitsEnum() );
        Layout layout = new HorizontalLayout();

        unitComboBox.select( memoryUnit.getShortName() );
        memoryTextField.setValue( memoryAvailable );
        layout.addComponent( memoryTextField );
        layout.addComponent( unitComboBox );

        setCompositionRoot( layout );
        setSizeFull();
    }


    public void setValueForMemoryTextField( String value )
    {
        memoryTextField.setValue( value );
    }


    public void setValueForMemoryUnitComboBox( MemoryUnit unit )
    {
        unitComboBox.select( unit.getShortName() );
    }


    private static IndexedContainer getUnitsEnum()
    {
        IndexedContainer units = new IndexedContainer();
        units.addContainerProperty( UNIT_SHORT_NAME, String.class, null );
        units.addContainerProperty( UNIT_LONG_NAME, String.class, null );

        for ( MemoryUnit anEnum : MemoryUnit.values() )
        {
            Item item = units.addItem( anEnum.getShortName() );

            Property shortProp = item.getItemProperty( UNIT_SHORT_NAME );
            String shortValue = ( String ) shortProp.getValue();
            shortValue = anEnum.getShortName();

            Property longProp = item.getItemProperty( UNIT_LONG_NAME );
            String longValue = ( String ) longProp.getValue();
            longValue = anEnum.getLongName();
        }
        return units;
    }
}
