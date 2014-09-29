package org.safehaus.subutai.common.util;


import java.util.UUID;

import org.doomdark.uuid.UUIDGenerator;


/**
 * Created by dilshat on 8/25/14.
 */
public class UUIDUtil
{

    public static UUID generateTimeBasedUUID()
    {
        return java.util.UUID.fromString( UUIDGenerator.getInstance().generateTimeBasedUUID().toString() );
    }


    public static UUID generateCassandraUUID()
    {
        return UUID.fromString( new com.eaio.uuid.UUID().toString() );
    }


    public static UUID generateMACBasedUUID()
    {
        return UUID.nameUUIDFromBytes( SysUtil.getMacAddress().getBytes() );
    }
}
