package org.safehaus.subutai.plugin.common.ui;


import org.safehaus.subutai.plugin.common.api.BaseManagerInterface;

import com.vaadin.ui.ProgressBar;


public abstract class BaseManager implements BaseManagerInterface
{

    public final static String CHECK_ALL_BUTTON_CAPTION = "Check All";
    public final static String START_ALL_BUTTON_CAPTION = "Start All";
    public final static String STOP_ALL_BUTTON_CAPTION = "Stop All";
    public final static String DESTROY_CLUSTER_BUTTON_CAPTION = "Destroy Cluster";
    public final static String ADD_NODE_BUTTON_CAPTION = "Add Node";
    public final static String CHECK_BUTTON_CAPTION = "Check";
    public final static String START_NAMENODE_BUTTON_CAPTION = "Start Namenode";
    public final static String START_JOBTRACKER_BUTTON_CAPTION = "Start JobTracker";
    public final static String STOP_NAMENODE_BUTTON_CAPTION = "Stop Namenode";
    public final static String STOP_JOBTRACKER_BUTTON_CAPTION = "Stop JobTracker";
    public final static String EXCLUDE_BUTTON_CAPTION = "Exclude";
    public final static String INCLUDE_BUTTON_CAPTION = "Include";
    public final static String DESTROY_BUTTON_CAPTION = "Destroy";
    public final static String URL_BUTTON_CAPTION = "URL";


    public final static String HOST_COLUMN_CAPTION = "Host";
    public final static String IP_COLUMN_CAPTION = "IP List";
    public final static String NODE_ROLE_COLUMN_CAPTION = "Node Role";
    public final static String STATUS_COLUMN_CAPTION = "Status";
    public final static String AVAILABLE_OPERATIONS_COLUMN_CAPTION = "AVAILABLE_OPERATIONS";
    public final static String DECOMMISSION_STATUS_CAPTION = "Decommission Status: ";
    public final static String START_STOP_BUTTON_DEFAULT_CAPTION = "Start/Stop";
    public final static String EXCLUDE_INCLUDE_BUTTON_DEFAULT_CAPTION = "Exclude/Include";


    private ProgressBar progressBar;
    private int processCount = 0;


    public BaseManager() {
        progressBar = new ProgressBar();
        progressBar.setIndeterminate( true );
        progressBar.setVisible( false );
    }

    protected synchronized void enableProgressBar() {
        incrementProcessCount();
        progressBar.setVisible( true );
    }


    protected synchronized void disableProgressBar() {
        if ( processCount > 0 ) {
            decrementProcessCount();
        }
        if ( processCount == 0 ) {
            progressBar.setVisible( false );
        }
    }


    protected synchronized void incrementProcessCount() {
        processCount++;
    }


    protected synchronized void decrementProcessCount() {
        processCount--;
    }
}
