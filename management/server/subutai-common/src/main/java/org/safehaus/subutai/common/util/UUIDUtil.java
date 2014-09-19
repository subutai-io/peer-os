package org.safehaus.subutai.common.util;


import java.util.UUID;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;


/**
 * Created by dilshat on 8/25/14.
 */
public class UUIDUtil
{

    public static UUID generateTimeBasedUUID()
    {
        // http://www.cowtowncoder.com/blog/archives/2010/10/entry_429.html
        /*
        // need to pass Ethernet address; can either use real one (shown here)
        EthernetAddress nic = EthernetAddress.fromInterface();
        // or bogus which would be gotten with: EthernetAddress.constructMulticastAddress()
        TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator( EthernetAddress.fromInterface() );
        // also: we don't specify synchronizer, getting an intra-JVM syncer; there is
        // also external file-locking-based synchronizer if multiple JVMs run JUG
        UUID uuid = uuidGenerator.generate();*/


        return Generators.timeBasedGenerator( EthernetAddress.fromInterface() ).generate();
    }


    public static UUID generateCassandraUUID()
    {
        return Generators.randomBasedGenerator().generate();
    }


    public static UUID generateStringUUID( String id )
    {
        return Generators.nameBasedGenerator().generate( id );
    }


    public static UUID generateMACBasedUUID()
    {
        return UUID.nameUUIDFromBytes( SysUtil.getMacAddress().getBytes() );
    }
}
