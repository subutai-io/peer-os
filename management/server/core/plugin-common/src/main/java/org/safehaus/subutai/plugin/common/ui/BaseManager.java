package org.safehaus.subutai.plugin.common.ui;


import java.util.List;
import java.util.UUID;

import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.BaseManagerInterface;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Table;


public abstract class BaseManager implements BaseManagerInterface
{

    public final static String CHECK_ALL_BUTTON_CAPTION = "Check All";
    public final static String START_ALL_BUTTON_CAPTION = "Start All";
    public final static String STOP_ALL_BUTTON_CAPTION = "Stop All";
    public final static String DESTROY_CLUSTER_BUTTON_CAPTION = "Destroy Cluster";
    public final static String ADD_NODE_BUTTON_CAPTION = "Add Node";
    public final static String CHECK_BUTTON_CAPTION = "Check";
    public final static String START_BUTTON_CAPTION = "Start";
    public final static String STOP_BUTTON_CAPTION = "Stop";
    public final static String DESTROY_BUTTON_CAPTION = "Destroy";
    public final static String HOST_COLUMN_CAPTION = "Host";
    public final static String IP_COLUMN_CAPTION = "IP List";
    public final static String NODE_ROLE_COLUMN_CAPTION = "Node Role";
    public final static String STATUS_COLUMN_CAPTION = "Status";
    public final static String AVAILABLE_OPERATIONS_COLUMN_CAPTION = "AVAILABLE_OPERATIONS";
    public final static String START_STOP_BUTTON_DEFAULT_CAPTION = "Start/Stop";

    protected GridLayout contentRoot;
    protected ProgressBar progressBar;
    protected int processCount = 0;


    public BaseManager()
    {
        contentRoot = new GridLayout();
        contentRoot.setSpacing( true );
        contentRoot.setMargin( true );
        contentRoot.setSizeFull();
        contentRoot.setRows( 40 );
        contentRoot.setColumns( 1 );

        progressBar = new ProgressBar();
        progressBar.setId( "indicator" );
        progressBar.setIndeterminate( true );
        progressBar.setVisible( false );
    }


    public synchronized void enableProgressBar()
    {
        incrementProcessCount();
        progressBar.setVisible( true );
    }


    public synchronized void disableProgressBar()
    {
        if ( processCount > 0 )
        {
            decrementProcessCount();
        }
        if ( processCount == 0 )
        {
            progressBar.setVisible( false );
        }
    }


    public synchronized void incrementProcessCount()
    {
        processCount++;
    }


    public synchronized void decrementProcessCount()
    {
        processCount--;
    }


    public HorizontalLayout getStatusLayout( final Item row )
    {
        if ( row == null )
        {
            return null;
        }
        return ( HorizontalLayout ) row.getItemProperty( STATUS_COLUMN_CAPTION ).getValue();
    }


    public Button getDestroyButton( final HorizontalLayout availableOperationsLayout )
    {
        if ( availableOperationsLayout == null )
        {
            return null;
        }
        else
        {
            for ( Component component : availableOperationsLayout )
            {
                if ( component.getCaption().equals( DESTROY_BUTTON_CAPTION ) )
                {
                    return ( Button ) component;
                }
            }
            return null;
        }
    }


    public Button getStartButton( final HorizontalLayout availableOperationsLayout )
    {
        if ( availableOperationsLayout == null )
        {
            return null;
        }
        else
        {
            for ( Component component : availableOperationsLayout )
            {
                if ( component.getCaption().contains( START_BUTTON_CAPTION ) )
                {
                    return ( Button ) component;
                }
            }
            return null;
        }
    }


    public Button getStopButton( final HorizontalLayout availableOperationsLayout )
    {
        if ( availableOperationsLayout == null )
        {
            return null;
        }
        else
        {
            for ( Component component : availableOperationsLayout )
            {
                if ( component.getCaption().contains( STOP_BUTTON_CAPTION ) )
                {
                    return ( Button ) component;
                }
            }
            return null;
        }
    }


    public Button getStartStopButton( final HorizontalLayout availableOperationsLayout )
    {
        if ( availableOperationsLayout == null )
        {
            return null;
        }
        else
        {
            for ( Component component : availableOperationsLayout )
            {
                if ( component.getCaption().contains( START_BUTTON_CAPTION ) || component.getCaption().contains(
                        STOP_BUTTON_CAPTION ) || component.getCaption().equals( START_STOP_BUTTON_DEFAULT_CAPTION ) )
                {
                    return ( Button ) component;
                }
            }
            return null;
        }
    }


    protected void populateTable( final Table table, List<ContainerHost> containerHosts )
    {

        table.removeAllItems();

        // Add UI components into relevant fields according to its role in cluster
        for ( final ContainerHost containerHost : containerHosts )
        {
            addRowComponents( table, containerHost );
        }
    }


    public Item getAgentRow( final Table table, final UUID agent )
    {

        int rowId = getAgentRowId( table, agent );
        Item row = null;
        if ( rowId >= 0 )
        {
            row = table.getItem( rowId );
        }
        if ( row == null )
        {
            Notification.show( "Agent rowId should have been found inside " + table.getCaption()
                    + " but could not find! " );
        }
        return row;
    }


    protected int getAgentRowId( final Table table, final UUID agent )
    {
        if ( table != null && agent != null )
        {
            for ( Object o : table.getItemIds() )
            {
                int rowId = ( Integer ) o;
                Item row = table.getItem( rowId );
                String hostName = row.getItemProperty( HOST_COLUMN_CAPTION ).getValue().toString();
                if ( hostName.equals( agent.toString() ) )
                {
                    return rowId;
                }
            }
        }
        return -1;
    }


    public void clickAllCheckButtons( Table table )
    {
        for ( Object o : table.getItemIds() )
        {
            int rowId = ( Integer ) o;
            Item row = table.getItem( rowId );
            HorizontalLayout availableOperationsLayout = getAvailableOperationsLayout( row );
            Button checkButton = getCheckButton( availableOperationsLayout );
            if ( checkButton != null )
            {
                checkButton.click();
            }
        }
    }


    public HorizontalLayout getAvailableOperationsLayout( Item row )
    {
        if ( row == null )
        {
            return null;
        }
        return ( HorizontalLayout ) ( row.getItemProperty( AVAILABLE_OPERATIONS_COLUMN_CAPTION ).getValue() );
    }


    public Button getCheckButton( final HorizontalLayout availableOperationsLayout )
    {
        if ( availableOperationsLayout == null )
        {
            return null;
        }
        else
        {
            for ( Component component : availableOperationsLayout )
            {
                if ( component.getCaption().equals( CHECK_BUTTON_CAPTION ) )
                {
                    return ( Button ) component;
                }
            }
            return null;
        }
    }


    public synchronized int getProcessCount()
    {
        return processCount;
    }


    public ProgressBar getProgressBar()
    {
        return progressBar;
    }
}
