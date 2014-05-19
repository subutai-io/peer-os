package org.safehaus.kiskis.mgmt.impl.flume;

import org.junit.Assert;
import org.junit.Test;

public class CommandsTest {

    @Test
    public void testMake() {
        for(CommandType t : CommandType.values()) {
            String s = Commands.make(t);
            Assert.assertNotNull("Empty command string", s);
            if(t != CommandType.STATUS)
                Assert.assertTrue(s.contains(t.toString().toLowerCase()));
        }
    }

}
