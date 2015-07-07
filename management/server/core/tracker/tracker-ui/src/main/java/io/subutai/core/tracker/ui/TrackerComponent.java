/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.subutai.core.tracker.ui;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.safehaus.subutai.common.tracker.OperationState;
import org.safehaus.subutai.common.tracker.TrackerOperationView;
import io.subutai.core.tracker.api.Tracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;


/**
 * Tracker Vaadin UI
 */
public class TrackerComponent extends CustomComponent
{

    private static final Logger LOG = LoggerFactory.getLogger( TrackerComponent.class.getName() );
    private static final String STATUS_PROPERTY = "Status";
    private final Tracker tracker;
    private final ExecutorService executor;
    private Table operationsTable;
    private TextArea outputTxtArea;
    private PopupDateField fromDate, toDate;
    private ComboBox sourcesCombo, limitCombo;
    private Date fromDateValue, toDateValue;
    private volatile UUID trackID;
    protected volatile boolean track = false;
    private volatile String source;
    private List<TrackerOperationView> currentOperations = new ArrayList<>();
    protected int limit = 10;


    public TrackerComponent( Tracker tracker, ExecutorService executor )
    {
        Preconditions.checkNotNull( tracker, "Tracker is null" );
        Preconditions.checkNotNull( executor, "Executor is null" );

        this.tracker = tracker;
        this.executor = executor;
        final GridLayout content = new GridLayout();
        content.setSpacing( true );
        content.setSizeFull();
        content.setMargin( true );
        content.setRows( 7 );
        content.setColumns( 1 );

        HorizontalLayout filterLayout = fillFilterLayout();
        operationsTable = createTableTemplate( "Operations" );
        getOutputTextArea();

        content.addComponent( filterLayout, 0, 0 );
        content.addComponent( operationsTable, 0, 1, 0, 3 );
        content.addComponent( outputTxtArea, 0, 4, 0, 6 );

        setCompositionRoot( content );
    }


    private HorizontalLayout fillFilterLayout()
    {
        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setSpacing( true );

        getSourcesCombo();
        generateDateFormat();
        getFromDateField();
        getToDateField();
        getLimitCombo();

        filterLayout.addComponent( sourcesCombo );
        filterLayout.addComponent( fromDate );
        filterLayout.addComponent( toDate );
        filterLayout.addComponent( limitCombo );
        return filterLayout;
    }


    private void getSourcesCombo()
    {
        sourcesCombo = new ComboBox( "Source" );
        sourcesCombo.setImmediate( true );
        sourcesCombo.setTextInputAllowed( false );
        sourcesCombo.setNullSelectionAllowed( false );
        sourcesCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            @Override
            public void valueChange( final Property.ValueChangeEvent event )
            {
                source = ( String ) event.getProperty().getValue();
                trackID = null;
                outputTxtArea.setValue( "" );
            }
        } );
    }


    private void generateDateFormat()
    {
        SimpleDateFormat df = new SimpleDateFormat( "ddMMyyyy HH:mm:ss" );
        Calendar cal = Calendar.getInstance();
        try
        {
            fromDateValue = df.parse( String.format( "%02d", cal.get( Calendar.DAY_OF_MONTH ) ) + String
                    .format( "%02d", cal.get( Calendar.MONTH ) + 1 ) + cal.get( Calendar.YEAR ) + " 00:00:00" );
            toDateValue = df.parse( String.format( "%02d", cal.get( Calendar.DAY_OF_MONTH ) ) + String
                    .format( "%02d", cal.get( Calendar.MONTH ) + 1 ) + cal.get( Calendar.YEAR ) + " 23:59:59" );
        }
        catch ( java.text.ParseException ex )
        {
            LOG.error( "Error in generateDateFormat", ex );
        }
    }


    private void getFromDateField()
    {
        fromDate = new PopupDateField();
        fromDate.setCaption( "From" );
        fromDate.setValue( fromDateValue );
        fromDate.setImmediate( true );
        fromDate.setDateFormat( "yyyy-MM-dd HH:mm:ss" );
        fromDate.addValueChangeListener( new Property.ValueChangeListener()
        {

            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() instanceof Date )
                {
                    fromDateValue = ( Date ) event.getProperty().getValue();
                }
            }
        } );
    }


    private void getToDateField()
    {
        toDate = new PopupDateField( "To", toDateValue );
        toDate.setImmediate( true );
        toDate.setDateFormat( "yyyy-MM-dd HH:mm:ss" );
        fromDate.setResolution( Resolution.SECOND );
        toDate.setResolution( Resolution.SECOND );
        toDate.addValueChangeListener( new Property.ValueChangeListener()
        {

            public void valueChange( Property.ValueChangeEvent event )
            {
                if ( event.getProperty().getValue() instanceof Date )
                {
                    toDateValue = ( Date ) event.getProperty().getValue();
                }
            }
        } );
    }


    private void getLimitCombo()
    {
        limitCombo = new ComboBox( "Show last", Arrays.asList( "10", "50", "100", "ALL" ) );
        limitCombo.setImmediate( true );
        limitCombo.setTextInputAllowed( false );
        limitCombo.setNullSelectionAllowed( false );
        limitCombo.setValue( limit );
        limitCombo.addValueChangeListener( new Property.ValueChangeListener()
        {
            public void valueChange( Property.ValueChangeEvent event )
            {
                onLimitValueChange( ( String ) event.getProperty().getValue() );
            }
        } );
    }


    protected void onLimitValueChange( String value )
    {
        limit = "ALL".equals( value ) ? Integer.MAX_VALUE : Integer.parseInt( value );
    }


    private Table createTableTemplate( String caption )
    {
        Table table = new Table( caption );
        table.setContainerDataSource( new IndexedContainer() );
        table.addContainerProperty( "Date", Date.class, null );
        table.addContainerProperty( "Operation", String.class, null );
        table.addContainerProperty( "Check", Button.class, null );
        table.addContainerProperty( STATUS_PROPERTY, Embedded.class, null );
        table.setSizeFull();
        table.setPageLength( 10 );
        table.setSelectable( false );
        table.setImmediate( true );
        return table;
    }


    private void getOutputTextArea()
    {
        outputTxtArea = new TextArea( "Operation output" );
        outputTxtArea.setSizeFull();
        outputTxtArea.setRows( 20 );
        outputTxtArea.setImmediate( true );
        outputTxtArea.setWordwrap( true );
    }


    public synchronized void startTracking()
    {
        if ( !isTrack() )
        {
            track = true;
            executor.execute( new Runnable()
            {
                @Override
                public void run()
                {
                    while ( isTrack() )
                    {
                        try
                        {
                            populateOperations();
                            populateLogs();
                            Thread.sleep( 1000 );
                        }
                        catch ( Exception e )
                        {
                            LOG.error( "Error in tracker", e );
                        }
                    }
                }
            } );
        }
    }


    public boolean isTrack()
    {
        return track;
    }


    protected void populateOperations()
    {
        if ( !Strings.isNullOrEmpty( source ) )
        {
            List<TrackerOperationView> operations =
                    tracker.getTrackerOperations( source, fromDateValue, toDateValue, limit );
            if ( operations.isEmpty() )
            {
                trackID = null;
                outputTxtArea.setValue( "" );
            }
            IndexedContainer container = ( IndexedContainer ) operationsTable.getContainerDataSource();
            currentOperations.removeAll( operations );

            for ( TrackerOperationView po : currentOperations )
            {
                container.removeItem( po.getId() );
            }

            boolean sortNeeded = false;
            for ( final TrackerOperationView po : operations )
            {
                sortNeeded |= populateOperation( container, po );
            }

            if ( sortNeeded )
            {
                Object[] properties = { "Date" };
                boolean[] ordering = { false };
                operationsTable.sort( properties, ordering );
            }

            currentOperations = operations;
        }
    }


    protected void setTrackID( final UUID trackID )
    {
        this.trackID = trackID;
    }


    private boolean populateOperation( final IndexedContainer container, final TrackerOperationView po )
    {
        boolean sortNeeded = false;
        Embedded progressIcon;
        if ( po.getState() == OperationState.RUNNING )
        {
            final String loadIconSource = "img/spinner.gif";
            progressIcon = new Embedded( "", new ThemeResource( loadIconSource ) );
        }
        else if ( po.getState() == OperationState.FAILED )
        {
            final String errorIconSource = "img/cancel.png";
            progressIcon = new Embedded( "", new ThemeResource( errorIconSource ) );
        }
        else
        {
            final String okIconSource = "img/ok.png";
            progressIcon = new Embedded( "", new ThemeResource( okIconSource ) );
        }

        Item item = container.getItem( po.getId() );
        if ( item == null )
        {
            final Button trackLogsBtn = new Button( "View logs" );
            trackLogsBtn.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    trackID = po.getId();
                }
            } );

            item = container.addItem( po.getId() );
            item.getItemProperty( "Date" ).setValue( po.getCreateDate() );
            item.getItemProperty( "Operation" ).setValue( po.getDescription() );
            item.getItemProperty( "Check" ).setValue( trackLogsBtn );
            item.getItemProperty( STATUS_PROPERTY ).setValue( progressIcon );

            sortNeeded = true;
        }
        else if ( item.getItemProperty( STATUS_PROPERTY ) != null )
        {
            Embedded embedded = ( Embedded ) item.getItemProperty( STATUS_PROPERTY ).getValue();
            if ( embedded != null && !( embedded.getSource().equals( progressIcon.getSource() ) ) )
            {
                item.getItemProperty( STATUS_PROPERTY ).setValue( progressIcon );
            }
        }
        return sortNeeded;
    }


    protected void populateLogs()
    {
        if ( trackID != null && !Strings.isNullOrEmpty( source ) )
        {
            TrackerOperationView po = tracker.getTrackerOperation( source, trackID );
            if ( po != null )
            {
                setOutput( po.getDescription() + "\nState: " + po.getState() + "\nLogs:\n" + po.getLog() );
                if ( po.getState() != OperationState.RUNNING )
                {
                    trackID = null;
                }
            }
            else
            {
                setOutput( "Operation not found. Check logs" );
            }
        }
    }


    private void setOutput( String output )
    {
        if ( !Strings.isNullOrEmpty( output ) )
        {
            outputTxtArea.setValue( output );
            outputTxtArea.setCursorPosition( outputTxtArea.getValue().length() - 1 );
        }
    }


    void refreshSources()
    {
        String oldSource = source;
        sourcesCombo.removeAllItems();
        List<String> sources = tracker.getTrackerOperationSources();
        for ( String src : sources )
        {
            sourcesCombo.addItem( src );
        }
        if ( !Strings.isNullOrEmpty( oldSource ) )
        {
            sourcesCombo.setValue( oldSource );
        }
        else if ( !sources.isEmpty() )
        {
            sourcesCombo.setValue( sources.iterator().next() );
        }
    }


    @Override
    public void detach()
    {
        super.detach();
        stopTracking();
    }


    @Override
    public void attach()
    {
        super.attach();
        startTracking();
    }


    protected void stopTracking()
    {
        track = false;
    }
}
