package org.safehaus.kiskis.mgmt.impl.solr;


import java.util.Set;

import com.google.common.collect.Sets;

import org.junit.BeforeClass;
import org.junit.Test;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.impl.solr.util.CommandRunnerMock;
import org.safehaus.kiskis.mgmt.impl.solr.util.TestUtil;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


public class CommandsTest {
    @BeforeClass
    public static void setUp() {
        Commands.init( new CommandRunnerMock() );
    }


    @Test
    public void getInstallCommand() {
        Set<Agent> agents = Sets.newHashSet( TestUtil.getAgent() );
        Command command = Commands.getInstallCommand( agents );

        assertNotNull( command );
        assertEquals( Commands.INSTALL, command.getDescription() );
    }


    @Test
    public void getStartCommand() {
        Command command = Commands.getStartCommand( TestUtil.getAgent() );

        assertNotNull( command );
        assertEquals( Commands.START, command.getDescription() );
    }


    @Test
    public void getStopCommand() {
        Command command = Commands.getStopCommand( TestUtil.getAgent() );

        assertNotNull( command );
        assertEquals( Commands.STOP, command.getDescription() );
    }


    @Test
    public void getStatusCommand() {
        Command command = Commands.getStatusCommand( TestUtil.getAgent() );

        assertNotNull( command );
        assertEquals( Commands.STATUS, command.getDescription() );
    }
}
