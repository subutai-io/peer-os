package org.safehaus.subutai.plugin.sqoop.ui.manager;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.sqoop.api.DataSourceType;
import org.safehaus.subutai.plugin.sqoop.api.Sqoop;
import org.safehaus.subutai.plugin.sqoop.api.setting.ImportParameter;
import org.safehaus.subutai.plugin.sqoop.api.setting.ImportSetting;

import com.vaadin.data.Property;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;


public class ImportPanel extends ImportExportBase
{

    private final Sqoop sqoop;
    private final ExecutorService executorService;
    DataSourceType type;
    CheckBox chkImportAllTables = new CheckBox( "Import all tables" );
    AbstractTextField hbaseTableNameField = UIUtil.getTextField( "Table name:", 300 );
    AbstractTextField hbaseColumnFamilyField = UIUtil.getTextField( "Column family:", 300 );
    AbstractTextField hiveDatabaseField = UIUtil.getTextField( "Database:", 300 );
    AbstractTextField hiveTableNameField = UIUtil.getTextField( "Table name:", 300 );


    public ImportPanel( Sqoop sqoop, ExecutorService executorService, Tracker tracker )
    {
        super( tracker );
        this.sqoop = sqoop;
        this.executorService = executorService;

        init();
    }


    public DataSourceType getType()
    {
        return type;
    }


    public void setType( DataSourceType type )
    {
        this.type = type;
        init();
    }


    @Override
    public ImportSetting makeSettings()
    {
        ImportSetting s = new ImportSetting();
        s.setType( type );
        s.setClusterName( clusterName );
        s.setHostname( host.getHostname() );
        s.setConnectionString( connStringField.getValue() );
        s.setTableName( tableField.getValue() );
        s.setUsername( usernameField.getValue() );
        s.setPassword( passwordField.getValue() );
        s.setOptionalParameters( optionalParams.getValue() );
        switch ( type )
        {
            case HDFS:
                s.addParameter( ImportParameter.IMPORT_ALL_TABLES, chkImportAllTables.getValue() );
                break;
            case HBASE:
                s.addParameter( ImportParameter.DATASOURCE_TABLE_NAME, hbaseTableNameField.getValue() );
                s.addParameter( ImportParameter.DATASOURCE_COLUMN_FAMILY, hbaseColumnFamilyField.getValue() );
                break;
            case HIVE:
                s.addParameter( ImportParameter.DATASOURCE_DATABASE, hiveDatabaseField.getValue() );
                s.addParameter( ImportParameter.DATASOURCE_TABLE_NAME, hiveTableNameField.getValue() );
                break;
            default:
                throw new AssertionError( type.name() );
        }
        return s;
    }


    @Override
    final void init()
    {
        removeAllComponents();

        if ( type == null )
        {
            VerticalLayout layout = new VerticalLayout();
            layout.addComponent( UIUtil.getLabel( "Select data source type<br/>", 200 ) );
            for ( final DataSourceType dst : DataSourceType.values() )
            {
                Button btn = UIUtil.getButton( dst.toString(), 100 );
                btn.addClickListener( new Button.ClickListener()
                {

                    @Override
                    public void buttonClick( Button.ClickEvent event )
                    {
                        setType( dst );
                    }
                } );
                layout.addComponent( btn );
            }
            Button btn = UIUtil.getButton( "Cancel", 100 );
            btn.addClickListener( new Button.ClickListener()
            {

                @Override
                public void buttonClick( Button.ClickEvent event )
                {
                    detachFromParent();
                }
            } );
            layout.addComponent( UIUtil.getLabel( "</br>", 100 ) );
            layout.addComponent( btn );
            addComponent( layout );
            return;
        }
        if ( host == null )
        {
            addComponent( UIUtil.getLabel( "<h1>No node selected</h1>", 200 ) );
            return;
        }

        super.init();
        chkImportAllTables.addValueChangeListener( new Property.ValueChangeListener()
        {

            @Override
            public void valueChange( Property.ValueChangeEvent e )
            {
                String v = e.getProperty().getValue().toString();
                tableField.setEnabled( !Boolean.parseBoolean( v ) );
            }
        } );

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent( UIUtil.getButton( "Import", 120, new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                clearLogMessages();
                if ( !checkFields() )
                {
                    return;
                }
                setFieldsEnabled( false );
                ImportSetting sett = makeSettings();
                final UUID trackId = sqoop.importData( sett );

                OperationWatcher watcher = new OperationWatcher( trackId );
                watcher.setCallback( new OperationCallback()
                {

                    @Override
                    public void onComplete()
                    {
                        setFieldsEnabled( true );
                    }
                } );
                executorService.execute( watcher );
            }
        } ) );
        buttons.addComponent( UIUtil.getButton( "Back", 120, new Button.ClickListener()
        {

            @Override
            public void buttonClick( Button.ClickEvent event )
            {
                reset();
                setType( null );
            }
        } ) );

        List<Component> ls = new ArrayList<>();
        ls.add( UIUtil.getLabel( "<h1>Sqoop Import</h1>", 100, Unit.PERCENTAGE ) );
        ls.add( UIUtil.getLabel( "<h1>" + type.toString() + "</h1>", 200 ) );
        ls.add( connStringField );
        ls.add( tableField );
        ls.add( usernameField );
        ls.add( passwordField );

        switch ( type )
        {
            case HDFS:
                ls.add( 3, chkImportAllTables );
                this.fields.add( chkImportAllTables );
                break;
            case HBASE:
                ls.add( UIUtil.getLabel( "<b>HBase parameters</b>", 200 ) );
                ls.add( hbaseTableNameField );
                ls.add( hbaseColumnFamilyField );
                this.fields.add( hbaseTableNameField );
                this.fields.add( hbaseColumnFamilyField );
                break;
            case HIVE:
                ls.add( 3, chkImportAllTables );
                ls.add( UIUtil.getLabel( "<b>Hive parameters</b>", 200 ) );
                ls.add( hiveDatabaseField );
                ls.add( hiveTableNameField );
                this.fields.add( chkImportAllTables );
                this.fields.add( hiveDatabaseField );
                this.fields.add( hiveTableNameField );
                break;
            default:
                throw new AssertionError( type.name() );
        }
        ls.add( optionalParams );
        ls.add( buttons );

        addComponents( ls );
    }


    @Override
    boolean checkFields()
    {
        if ( super.checkFields() )
        {
            switch ( type )
            {
                case HDFS:
                    if ( !isChecked( chkImportAllTables ) )
                    {
                        if ( !hasValue( tableField, "Table name not specified" ) )
                        {
                            return false;
                        }
                    }
                    break;
                case HBASE:
                    if ( !hasValue( hbaseTableNameField, "HBase table name not specified" ) )
                    {
                        return false;
                    }
                    if ( !hasValue( hbaseColumnFamilyField, "HBase column family not specified" ) )
                    {
                        return false;
                    }
                    break;
                case HIVE:
                    if ( !isChecked( chkImportAllTables ) )
                    {
                        if ( !hasValue( tableField, "Table name not specified" ) )
                        {
                            return false;
                        }
                    }
                    if ( !hasValue( hiveDatabaseField, "Hive database not specified" ) )
                    {
                        return false;
                    }
                    if ( !hasValue( hiveTableNameField, "Hive table name not specified" ) )
                    {
                        return false;
                    }
                    break;
                default:
                    throw new AssertionError( type.name() );
            }
            return true;
        }
        return false;
    }


    private boolean isChecked( CheckBox chb )
    {
        Object v = chb.getValue();
        return v != null ? Boolean.parseBoolean( v.toString() ) : false;
    }
}
