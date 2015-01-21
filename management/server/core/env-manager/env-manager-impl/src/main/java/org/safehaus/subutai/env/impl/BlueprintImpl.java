package org.safehaus.subutai.env.impl;


import java.util.Collections;
import java.util.Set;

import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.env.api.build.Blueprint;
import org.safehaus.subutai.env.api.build.NodeGroup;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Environment Blueprint
 */
public class BlueprintImpl implements Blueprint
{
    private String name;
    private Set<NodeGroup> nodeGroups;


    public BlueprintImpl( final String name, final Set<NodeGroup> nodeGroups )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid name" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( nodeGroups ), "Invalid node group set" );

        this.name = name;
        this.nodeGroups = nodeGroups;
    }


    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public Set<NodeGroup> getNodeGroups()
    {
        return Collections.unmodifiableSet( nodeGroups );
    }
}
