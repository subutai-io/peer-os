package io.subutai.core.metric.impl;


import java.util.Collections;
import java.util.Comparator;

import io.subutai.common.peer.AlertHandler;


/**
 * Alert handler comparator.
 */
public class AlertHandlerComparator implements Comparator<AlertHandler>
{
    @Override
    public int compare( final AlertHandler o1, final AlertHandler o2 )
    {
        int result = o2.getAlertHandlerPriority().compareTo( o1.getAlertHandlerPriority() );

        if ( result == 0 )
        {
            result = o1.getHandlerId().compareToIgnoreCase( o2.getHandlerId() );
        }
        return result;
    }
}
