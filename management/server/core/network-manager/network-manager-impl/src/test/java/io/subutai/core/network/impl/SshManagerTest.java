package io.subutai.core.network.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ContainerHost;
import io.subutai.core.network.api.NetworkManagerException;

import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class SshManagerTest
{
    private static final String SSH_KEY = "KEY";
    @Mock
    ContainerHost containerHost;
    @Mock
    CommandUtil commandUtil;
    @Mock
    CommandResult result;


    private SshManager sshManager;


    @Before
    public void setUp() throws Exception
    {
        sshManager = new SshManager( Sets.newHashSet( containerHost ) );
        when( commandUtil.execute( any( RequestBuilder.class ), any( ContainerHost.class ) ) ).thenReturn( result );
        when( result.getStdOut() ).thenReturn( SSH_KEY );
        sshManager.commandUtil = commandUtil;
        sshManager.keys = Lists.newArrayList( SSH_KEY );
    }


    private void verifyCommandUtilExec() throws CommandException
    {
        verify( commandUtil, atLeastOnce() ).execute( any( RequestBuilder.class ), eq( containerHost ) );
    }


    private void verifyHostExec() throws CommandException
    {
        verify( containerHost ).execute( any( RequestBuilder.class ) );
    }


    private void throwCommandException() throws CommandException
    {
        doThrow( new CommandException( "" ) ).when( commandUtil )
                                             .execute( any( RequestBuilder.class ), eq( containerHost ) );
        doThrow( new CommandException( "" ) ).when( containerHost ).execute( any( RequestBuilder.class ) );
    }


    @Test( expected = NetworkManagerException.class )
    public void testAppendSshKey() throws Exception
    {
        sshManager.appendSshKey( SSH_KEY );

        verifyCommandUtilExec();

        throwCommandException();

        sshManager.appendSshKey( SSH_KEY );
    }


    @Test( expected = NetworkManagerException.class )
    public void testReplaceSshKey() throws Exception
    {
        sshManager.replaceSshKey( SSH_KEY, SSH_KEY );

        verifyCommandUtilExec();

        throwCommandException();

        sshManager.replaceSshKey( SSH_KEY, SSH_KEY );
    }


    @Test( expected = NetworkManagerException.class )
    public void testRemoveSshKey() throws Exception
    {
        sshManager.removeSshKey( SSH_KEY );

        verifyHostExec();

        throwCommandException();

        sshManager.removeSshKey( SSH_KEY );
    }


    @Test( expected = NetworkManagerException.class )
    public void testCreate() throws Exception
    {
        sshManager.create();

        verifyCommandUtilExec();

        throwCommandException();

        sshManager.create();
    }


    @Test( expected = NetworkManagerException.class )
    public void testRead() throws Exception
    {
        sshManager.read();

        verifyCommandUtilExec();

        sshManager.keys.clear();
        reset( result );

        try
        {
            sshManager.read();

            fail( "Expected NetworkManagerException" );
        }
        catch ( NetworkManagerException e )
        {

        }

        throwCommandException();

        sshManager.read();
    }


    @Test( expected = NetworkManagerException.class )
    public void testWrite() throws Exception
    {
        sshManager.write();

        verifyCommandUtilExec();

        throwCommandException();

        sshManager.write();
    }


    @Test( expected = NetworkManagerException.class )
    public void testConfig() throws Exception
    {
        sshManager.config();

        verifyCommandUtilExec();

        throwCommandException();

        sshManager.config();
    }


    @Test
    public void testExecute() throws Exception
    {
        sshManager.execute();

        verifyCommandUtilExec();
    }
}
