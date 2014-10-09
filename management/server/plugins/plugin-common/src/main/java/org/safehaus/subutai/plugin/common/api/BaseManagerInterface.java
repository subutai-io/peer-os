package org.safehaus.subutai.plugin.common.api;


import org.safehaus.subutai.common.protocol.Agent;

import com.vaadin.ui.Table;


public interface BaseManagerInterface
{
    public void refreshClustersInfo();

    public abstract void addRowComponents( Table table, final Agent agent );

    public abstract Table createTableTemplate( String caption );
}
