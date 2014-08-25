package org.safehaus.subutai.common;


import java.util.UUID;

import org.doomdark.uuid.UUIDGenerator;


/**
 * Created by dilshat on 8/25/14.
 */
public class UUIDUtil {

    public static UUID generateTimeBasedUUID() {
   		return java.util.UUID.fromString( UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
   	}
}
