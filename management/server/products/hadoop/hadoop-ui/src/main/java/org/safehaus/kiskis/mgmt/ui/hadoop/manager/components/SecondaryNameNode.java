package org.safehaus.kiskis.mgmt.ui.hadoop.manager.components;

import org.safehaus.kiskis.mgmt.api.hadoop.Config;

import java.util.UUID;

/**
 * Created by daralbaev on 12.04.14.
 */
public class SecondaryNameNode extends ClusterNode {

    public SecondaryNameNode(Config cluster) {
        super(cluster);
    }

    @Override
    public void getStatus(UUID trackID) {

    }
}
