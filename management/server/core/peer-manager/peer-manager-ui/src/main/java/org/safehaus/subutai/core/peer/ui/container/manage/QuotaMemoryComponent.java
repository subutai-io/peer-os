package org.safehaus.subutai.core.peer.ui.container.manage;


import java.math.BigInteger;

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

                performConversion( newUnit );
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


    public String getMemoryLimitValue()
    {
        String value = memoryTextField.getValue().replaceAll( "\n", "" );
        BigInteger memory = new BigInteger( value );
        while ( defaultUnit.getValue() > 0 )
        {
            memory = convertToLess( memory );
            defaultUnit = MemoryUnit.getMemoryUnit( defaultUnit.getValue() - 1 );
        }
        return memory.toString();
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


    private void performConversion( MemoryUnit newValue )
    {
        String conversionResult = "";
        String str = memoryTextField.getValue().replaceAll( "\n", "" );
        LOGGER.warn( str );
        BigInteger value = new BigInteger( str );

        BigInteger currentUnitValue = new BigInteger( str );
        if ( defaultUnit.getValue() < newValue.getValue() )
        {
            while ( defaultUnit.getValue() < newValue.getValue() )
            {
                currentUnitValue = convertToBigger( currentUnitValue );
                conversionResult = String.valueOf( currentUnitValue );
                defaultUnit = MemoryUnit.getMemoryUnit( defaultUnit.getValue() + 1 );
            }
        }
        else if ( defaultUnit.getValue() > newValue.getValue() )
        {
            while ( defaultUnit.getValue() > newValue.getValue() )
            {
                currentUnitValue = convertToLess( currentUnitValue );
                conversionResult = String.valueOf( currentUnitValue );
                defaultUnit = MemoryUnit.getMemoryUnit( defaultUnit.getValue() - 1 );
            }
        }
        memoryTextField.setValue( conversionResult );
    }


    private BigInteger convertToBigger( BigInteger value )
    {
        return value.divide( new BigInteger( "1024" ) );
    }


    private BigInteger convertToLess( BigInteger value )
    {
        return value.multiply( new BigInteger( "1024" ) );
    }
}
