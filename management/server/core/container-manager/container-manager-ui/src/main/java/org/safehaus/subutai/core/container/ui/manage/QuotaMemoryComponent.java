package org.safehaus.subutai.core.container.ui.manage;


import org.safehaus.subutai.core.lxc.quota.api.MemoryUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


/**
 * Created by talas on 10/8/14.
 */
public class QuotaMemoryComponent extends VerticalLayout
{

    private static Logger LOGGER = LoggerFactory.getLogger( QuotaMemoryComponent.class );
    private TextField memoryTextField;
    private ComboBox unitComboBox;
    private MemoryUnit defaultUnit = MemoryUnit.BYTES;

    private static final String UNIT_SHORT_NAME = "ShortName";
    private static final String UNIT_LONG_NAME = "LongName";


    public QuotaMemoryComponent()
    {
        setHeight( 100, Unit.PERCENTAGE );
        setWidth( 100, Unit.PERCENTAGE );

        memoryTextField = new TextField();
        memoryTextField.setWidth( 100, Unit.PERCENTAGE );
        memoryTextField.setValue( "0" );
        addComponent( memoryTextField );

        unitComboBox = new ComboBox( "", getUnitsEnum() );
        //        unitComboBox.setWidth( 50, Unit.PERCENTAGE );
        //        unitComboBox.select( MemoryUnit.BYTES.getShortName() );

        unitComboBox.setItemCaptionPropertyId( UNIT_LONG_NAME );
        unitComboBox.setItemCaptionMode( AbstractSelect.ItemCaptionMode.PROPERTY );

        unitComboBox.select( defaultUnit.getShortName() );

        unitComboBox.setInputPrompt( "Select unit." );
        unitComboBox.setNullSelectionAllowed( false );

        unitComboBox.setFilteringMode( FilteringMode.CONTAINS );
        unitComboBox.setImmediate( true );
        unitComboBox.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( final Property.ValueChangeEvent valueChangeEvent )
            {
                Item item = unitComboBox.getItem( valueChangeEvent.getProperty().getValue() );
                String unitId = String.valueOf( item.getItemProperty( UNIT_SHORT_NAME ).getValue() );
                LOGGER.warn( String.valueOf( item.getItemProperty( UNIT_SHORT_NAME ).getValue() ) );

                MemoryUnit newUnit = MemoryUnit.getMemoryUnit( unitId );

                performConversion( defaultUnit, newUnit );
            }
        } );
        unitComboBox.select( getUnitsEnum().getItem( MemoryUnit.BYTES.getShortName() ) );
        addComponent( unitComboBox );

        setSizeFull();
    }


    public void setValueForMemoryTextField( String value )
    {
        memoryTextField.setValue( value );
    }


    public void setValueForMemoryUnitComboBox( MemoryUnit unit )
    {
        //        unitComboBox.select( unit.getShortName() );
    }


    private static IndexedContainer getUnitsEnum()
    {
        IndexedContainer units = new IndexedContainer();
        units.addContainerProperty( UNIT_SHORT_NAME, String.class, null );
        units.addContainerProperty( UNIT_LONG_NAME, String.class, null );

        for ( MemoryUnit anEnum : MemoryUnit.values() )
        {
            Item item = units.addItem( anEnum.getShortName() );

            item.getItemProperty( UNIT_SHORT_NAME ).setValue( anEnum.getShortName() );
            item.getItemProperty( UNIT_LONG_NAME ).setValue( anEnum.getLongName() );

            LOGGER.info( anEnum.getShortName() + "  " + anEnum.getLongName() );
        }
        return units;
    }


    private void performConversion( MemoryUnit oldValue, MemoryUnit newValue )
    {
        String conversionResult = "";
        String str = memoryTextField.getValue().replaceAll( "\n", "" );
        LOGGER.warn( str );
        Long currentUnitValue = Long.parseLong( str );
        if ( oldValue.getValue() < newValue.getValue() )
        {
            while ( oldValue.getValue() < newValue.getValue() )
            {
                switch ( oldValue.getValue() )
                {
                    case 0:
                        conversionResult = String.valueOf( convertToBigger( currentUnitValue ) );
                        break;
                    case 1:
                        conversionResult = String.valueOf( convertToBigger( currentUnitValue ) );
                        break;
                    case 2:
                        conversionResult = String.valueOf( convertToBigger( currentUnitValue ) );
                }
                oldValue = MemoryUnit.getMemoryUnit( oldValue.getValue() + 1 );
            }
        }
        else if ( oldValue.getValue() > newValue.getValue() )
        {
            while ( oldValue.getValue() > newValue.getValue() )
            {
                switch ( oldValue.getValue() )
                {
                    case 0:
                        conversionResult = String.valueOf( convertToLess( currentUnitValue ) );
                        break;
                    case 1:
                        conversionResult = String.valueOf( convertToLess( currentUnitValue ) );
                        break;
                    case 2:
                        conversionResult = String.valueOf( convertToLess( currentUnitValue ) );
                }
                oldValue = MemoryUnit.getMemoryUnit( oldValue.getValue() - 1 );
            }
        }
        memoryTextField.setValue( conversionResult );
    }


    private long convertToBigger( long value )
    {
        return value / 1024;
    }


    private long convertToLess( long value )
    {
        return value * 1024;
    }
}
