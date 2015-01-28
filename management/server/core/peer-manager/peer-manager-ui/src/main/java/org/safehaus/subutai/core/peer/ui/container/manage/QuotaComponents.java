package org.safehaus.subutai.core.peer.ui.container.manage;


import java.math.BigInteger;

import org.safehaus.subutai.common.quota.MemoryUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;


/**
 * Created by ermek on 10/31/14.
 */
public class QuotaComponents extends GridLayout
{
    private static Logger LOGGER = LoggerFactory.getLogger( QuotaComponents.class );
    private TextField memoryTextField2;
    private TextField lblmemoryTextField2;
    private ComboBox unitComboBox2;
    private TextField lblCoresUsed;
    private TextField coresUsedTextField;
    private MemoryUnit defaultUnit2 = MemoryUnit.BYTES;

    private static final String UNIT_SHORT_NAME = "ShortName";
    private static final String UNIT_LONG_NAME = "LongName";


    public QuotaComponents()
    {
        setImmediate( false );
        setWidth( "100.0%" );
        setHeight( "200px" );
        setMargin( false );
        setSpacing( true );
        setColumns( 3 );
        setRows( 2 );

        // label for memory setting
        lblmemoryTextField2 = new TextField();
        lblmemoryTextField2.setImmediate( false );
        lblmemoryTextField2.setWidth( "-1px" );
        lblmemoryTextField2.setHeight( "-1px" );
        lblmemoryTextField2.setValue( "Memory" );
        lblmemoryTextField2.setReadOnly( true );
        addComponent( lblmemoryTextField2, 0, 0 );
        setComponentAlignment( lblmemoryTextField2, new Alignment( 48 ) );

        // combobox for memory setting
        unitComboBox2 = new ComboBox( "", getUnitsEnum() );
        unitComboBox2.setImmediate( false );
        unitComboBox2.setWidth( "-1px" );
        unitComboBox2.setHeight( "-1px" );

        unitComboBox2.setItemCaptionPropertyId( UNIT_LONG_NAME );
        unitComboBox2.setItemCaptionMode( AbstractSelect.ItemCaptionMode.PROPERTY );

        unitComboBox2.select( defaultUnit2.getAcronym() );

        unitComboBox2.setInputPrompt( "Select unit." );
        unitComboBox2.setNullSelectionAllowed( false );

        unitComboBox2.setFilteringMode( FilteringMode.CONTAINS );
        unitComboBox2.setImmediate( true );
        unitComboBox2.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( final Property.ValueChangeEvent valueChangeEvent )
            {
                Item item = unitComboBox2.getItem( valueChangeEvent.getProperty().getValue() );
                String unitId = String.valueOf( item.getItemProperty( UNIT_SHORT_NAME ).getValue() );
                LOGGER.warn( String.valueOf( item.getItemProperty( UNIT_SHORT_NAME ).getValue() ) );

                MemoryUnit newUnit = MemoryUnit.getMemoryUnit( unitId );

                performConversion( newUnit );
            }
        } );
        unitComboBox2.select( getUnitsEnum().getItem( MemoryUnit.BYTES.getAcronym() ) );
        //addComponent( unitComboBox2 );
        addComponent( unitComboBox2, 1, 0 );
        setComponentAlignment( unitComboBox2, new Alignment( 33 ) );


        // textField for memory setting
        memoryTextField2 = new TextField();
        memoryTextField2.setImmediate( false );
        memoryTextField2.setWidth( "-1px" );
        memoryTextField2.setHeight( "-1px" );
        addComponent( memoryTextField2, 2, 0 );
        setComponentAlignment( memoryTextField2, new Alignment( 33 ) );

        // label for cores used textfield
        lblCoresUsed = new TextField();
        lblCoresUsed.setImmediate( false );
        lblCoresUsed.setWidth( "-1px" );
        lblCoresUsed.setHeight( "-1px" );
        lblCoresUsed.setValue( "Core used" );
        lblCoresUsed.setReadOnly( true );
        addComponent( lblCoresUsed, 0, 1 );
        setComponentAlignment( lblCoresUsed, new Alignment( 48 ) );

        // coresUsedTextField
        coresUsedTextField = new TextField();
        coresUsedTextField.setImmediate( false );
        coresUsedTextField.setWidth( "-1px" );
        coresUsedTextField.setHeight( "-1px" );
        addComponent( coresUsedTextField, 1, 1 );
        setComponentAlignment( coresUsedTextField, new Alignment( 33 ) );
    }


    public void setValueForMemoryTextField2( String value )
    {
        memoryTextField2.setValue( value );
    }


    public void setValueForCoresUsedTextField( String value )
    {
        coresUsedTextField.setValue( value );
    }


    public String getMemoryLimitValue()
    {
        String value = memoryTextField2.getValue().replaceAll( "\n", "" );
        BigInteger memory = new BigInteger( value );
        while ( defaultUnit2.getUnitIdx() > 0 )
        {
            memory = convertToLess( memory );
            defaultUnit2 = MemoryUnit.getMemoryUnit( defaultUnit2.getUnitIdx() - 1 );
        }
        return memory.toString();
    }


    public String getValueFromCpuCoresUsed()
    {
        String value = coresUsedTextField.getValue().replaceAll( "\n", "" );
        return value;
    }


    private static IndexedContainer getUnitsEnum()
    {
        IndexedContainer units = new IndexedContainer();
        units.addContainerProperty( UNIT_SHORT_NAME, String.class, null );
        units.addContainerProperty( UNIT_LONG_NAME, String.class, null );

        for ( MemoryUnit anEnum : MemoryUnit.values() )
        {
            Item item = units.addItem( anEnum.getAcronym() );

            item.getItemProperty( UNIT_SHORT_NAME ).setValue( anEnum.getAcronym() );
            item.getItemProperty( UNIT_LONG_NAME ).setValue( anEnum.getName() );

            LOGGER.info( anEnum.getAcronym() + "  " + anEnum.getName() );
        }
        return units;
    }


    private void performConversion( MemoryUnit newValue )
    {
        String conversionResult = "";
        String str = memoryTextField2.getValue().replaceAll( "\n", "" );
        LOGGER.warn( str );
        BigInteger value = new BigInteger( str );

        BigInteger currentUnitValue = new BigInteger( str );
        if ( defaultUnit2.getUnitIdx() < newValue.getUnitIdx() )
        {
            while ( defaultUnit2.getUnitIdx() < newValue.getUnitIdx() )
            {
                currentUnitValue = convertToBigger( currentUnitValue );
                conversionResult = String.valueOf( currentUnitValue );
                defaultUnit2 = MemoryUnit.getMemoryUnit( defaultUnit2.getUnitIdx() + 1 );
            }
        }
        else if ( defaultUnit2.getUnitIdx() > newValue.getUnitIdx() )
        {
            while ( defaultUnit2.getUnitIdx() > newValue.getUnitIdx() )
            {
                currentUnitValue = convertToLess( currentUnitValue );
                conversionResult = String.valueOf( currentUnitValue );
                defaultUnit2 = MemoryUnit.getMemoryUnit( defaultUnit2.getUnitIdx() - 1 );
            }
        }
        memoryTextField2.setValue( conversionResult );
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
