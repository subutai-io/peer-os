package org.safehaus.subutai.core.peer.impl.task;


import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.task.clone.CloneTask;


/**
 * Created by timur on 11/30/14.
 */
public class CloneTaskImpl extends BaseTaskImpl<CloneTask>
{
    LocalPeer localPeer;


    public CloneTaskImpl( CloneTask task )
    {
        super( task );
    }


    public void setLocalPeer( LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    public void run()
    {

    }
}