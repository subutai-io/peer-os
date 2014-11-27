/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.strategy.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.core.strategy.api.ContainerPlacementStrategy;
import org.safehaus.subutai.core.strategy.api.ServerMetric;

import com.google.common.collect.Sets;


/**
 * Mocking utilities
 */
public class MockUtils
{

    private static final UUID physicalUUID_1 = UUID.randomUUID();
    private static final UUID physicalUUID_2 = UUID.randomUUID();
    private static final UUID physicalUUID_3 = UUID.randomUUID();
    private static final UUID lxcUUID_1 = UUID.randomUUID();
    private static final UUID lxcUUID_2 = UUID.randomUUID();
    private static final UUID lxcUUID_3 = UUID.randomUUID();
    private static final String PHYSICAL_HOSTNAME_1 = "py111";
    private static final String PHYSICAL_HOSTNAME_2 = "py222";
    private static final String PHYSICAL_HOSTNAME_3 = "py333";
    private static final String LXC_HOSTNAME_1 = "py111-lxc-111";
    private static final String LXC_HOSTNAME_2 = "py111-lxc-222";
    private static final String LXC_HOSTNAME_3 = "py111-lxc-333";


    public static ContainerPlacementStrategy getDefaultContainerPlacementStrategy()
    {
        return new DefaultContainerPlacementStrategy();
    }


    public static ContainerPlacementStrategy getRoundRobinPlacementStrategy()
    {
        return new RoundRobinStrategy();
    }


    public static ContainerPlacementStrategy getBestServerPlacementStrategy()
    {
        return new BestServerStrategy();
    }


    public static List<ServerMetric> getServerMetrics()
    {
        List<ServerMetric> result = new ArrayList<>();


        Set<String> hosts = Sets.newHashSet( PHYSICAL_HOSTNAME_1, PHYSICAL_HOSTNAME_2, PHYSICAL_HOSTNAME_3 );
        int i = 0;
        for ( String hostname : hosts )
        {
            ServerMetric metric =
                    new ServerMetric( hostname, 20000 + ( i++ ), 40000 + ( i++ ), 30 + ( i++ ), 4 + ( i++ ) );
            result.add( metric );
        }
        return result;
    }
}
