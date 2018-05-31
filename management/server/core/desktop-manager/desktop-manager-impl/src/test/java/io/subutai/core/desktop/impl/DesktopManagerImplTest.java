package io.subutai.core.desktop.impl;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandResultImpl;
import io.subutai.common.command.CommandStatus;
import io.subutai.common.peer.ContainerHost;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


@RunWith( MockitoJUnitRunner.class )
public class DesktopManagerImplTest
{
    private final Logger log = LoggerFactory.getLogger( getClass() );
    public static final String RESULT_DESK = "Unity";
    public static final String RESULT_RDS = "x2goserver";

    private DesktopManagerImpl desktopManager;

    @Mock
    ContainerHost containerHost;


    @Before
    public void setUp() throws Exception
    {
        CommandResult deskCommandResult = new CommandResultImpl( 202, RESULT_DESK, null, CommandStatus.SUCCEEDED );
        CommandResult rdsCommandResult = new CommandResultImpl( 202, RESULT_RDS, null, CommandStatus.SUCCEEDED );
        desktopManager = spy( new DesktopManagerImpl() );
        doReturn( deskCommandResult ).when( containerHost ).execute( Commands.getDeskEnvSpecifyCommand() );
        doReturn( rdsCommandResult ).when( containerHost ).execute( Commands.getRDServerSpecifyCommand() );
    }


    @Test
    @Ignore
    public void isDesktop() throws Exception
    {
        boolean isDekstop = desktopManager.isDesktop( containerHost );
        assertTrue( isDekstop );
    }


    @Test
    public void getDesktopEnvironmentInfo() throws Exception
    {
        String deskEnv = desktopManager.getDesktopEnvironmentInfo( containerHost );
        assertEquals( RESULT_DESK, deskEnv );
    }


    @Test
    public void getRDServerInfo() throws Exception
    {
        String rdServer = desktopManager.getRDServerInfo( containerHost );
        assertEquals( RESULT_RDS, rdServer );
    }
}
