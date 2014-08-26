package org.safehaus.subutai.impl.flume.handler;

import org.junit.Assert;
import org.junit.Test;
import org.safehaus.subutai.impl.flume.CommandType;
import org.safehaus.subutai.impl.flume.Commands;

public class CommandsTest {

	@Test
	public void testMake() {
		for (CommandType t : CommandType.values()) {
			String s = Commands.make(t);
			Assert.assertNotNull("Empty command string", s);
			if (t != CommandType.STATUS)
				Assert.assertTrue(s.contains(t.toString().toLowerCase()));
		}
	}

}
