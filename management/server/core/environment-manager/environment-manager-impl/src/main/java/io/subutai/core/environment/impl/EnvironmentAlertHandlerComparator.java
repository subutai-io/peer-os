package io.subutai.core.environment.impl;


import java.util.Comparator;

import io.subutai.common.peer.EnvironmentAlertHandler;


/**
 * AlertHandlerId comparator
 */
public class EnvironmentAlertHandlerComparator implements Comparator<EnvironmentAlertHandler>
{
    @Override
    public int compare( final EnvironmentAlertHandler o1, final EnvironmentAlertHandler o2 )
    {
        int result = o2.getAlertHandlerPriority().compareTo( o1.getAlertHandlerPriority() );

        if ( result == 0 )
        {
            result = o1.getAlertHandlerId().compareToIgnoreCase( o2.getAlertHandlerId());
        }
        return result;
    }
}
