package org.safehaus.subutai.plugin.common.ui;


import org.safehaus.subutai.plugin.common.api.BaseManagerInterface;

import com.vaadin.ui.ProgressBar;


public abstract class BaseManager implements BaseManagerInterface
{
    private ProgressBar progressBar;
    private int processCount = 0;


    public BaseManager() {
        progressBar = new ProgressBar();
        progressBar.setIndeterminate( true );
        progressBar.setVisible( false );
    }

    protected synchronized void enableProgressBar() {
        processCount++;
        progressBar.setVisible( true );
    }


    protected synchronized void disableProgressBar() {
        if ( processCount > 0 ) {
            processCount--;
        }
        if ( processCount == 0 ) {
            progressBar.setVisible( false );
        }
    }
}
