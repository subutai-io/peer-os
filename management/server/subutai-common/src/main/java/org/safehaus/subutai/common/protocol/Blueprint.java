package org.safehaus.subutai.common.protocol;


import java.util.UUID;

import org.safehaus.subutai.common.util.UUIDUtil;


/**
 * Created by bahadyr on 10/14/14.
 */
public abstract class Blueprint
{
    protected UUID uuid;


    public Blueprint()
    {
        this.uuid = UUIDUtil.generateTimeBasedUUID();
    }


    public UUID getUuid()
    {
        return uuid;
    }


    public void setUuid( final UUID uuid )
    {
        this.uuid = uuid;
    }
}
