package org.safehaus.subutai.common.util;


import org.doomdark.uuid.UUIDGenerator;

import java.util.UUID;


/**
 * Created by dilshat on 8/25/14.
 */
public class UUIDUtil
{

    public static UUID generateTimeBasedUUID()
    {
        return java.util.UUID.fromString( UUIDGenerator.getInstance().generateTimeBasedUUID().toString() );
    }
}
