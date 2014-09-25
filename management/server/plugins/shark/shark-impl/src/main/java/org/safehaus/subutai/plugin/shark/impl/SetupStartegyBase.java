package org.safehaus.subutai.plugin.shark.impl;


import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;


public class SetupStartegyBase
{
    final SharkImpl manager;
    final SharkClusterConfig config;
    final ProductOperation po;


    public SetupStartegyBase( SharkImpl manager, SharkClusterConfig config, ProductOperation po )
    {
        this.manager = manager;
        this.config = config;
        this.po = po;
    }


}

