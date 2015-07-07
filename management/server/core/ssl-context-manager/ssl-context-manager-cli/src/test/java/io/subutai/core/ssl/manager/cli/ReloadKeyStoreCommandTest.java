package io.subutai.core.ssl.manager.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.core.ssl.manager.api.SubutaiSslContextFactory;
import io.subutai.core.ssl.manager.cli.ReloadKeyStoreCommand;

import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ReloadKeyStoreCommandTest
{

    ReloadKeyStoreCommand reloadKeyStoreCommand;

    @Mock
    SubutaiSslContextFactory subutaiSslContextFactory;


    @Before
    public void setUp() throws Exception
    {
        when( subutaiSslContextFactory.getSSLContext() ).thenReturn( "Subutai Context Factory" );
        reloadKeyStoreCommand = new ReloadKeyStoreCommand( subutaiSslContextFactory );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        reloadKeyStoreCommand.doExecute();
    }
}