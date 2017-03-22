package io.subutai.core.localpeer.impl.command;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.Response;
import io.subutai.core.localpeer.impl.entity.ResourceHostEntity;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class TemplateDownloadTrackerTest
{
    private static final String ENV_ID = UUID.randomUUID().toString();

    @Mock
    ResourceHostEntity resourceHostEntity;
    @Mock
    CommandResult commandResult;
    @Mock
    Response response;

    private TemplateDownloadTracker tracker;


    @Before
    public void setUp() throws Exception
    {
        doReturn( "" ).when( response ).getStdOut();
        doReturn( "" ).when( commandResult ).getStdOut();
        tracker = spy( new TemplateDownloadTracker( resourceHostEntity, ENV_ID ) );
    }


    @Test
    public void testInstanceExist() throws Exception
    {
        doReturn( "time=\"2017-03-22 12:10:08\" level=info msg=\"Importing apache\" \n"
                + "time=\"2017-03-22 12:10:08\" level=info msg=\"apache instance exist\" " ).when( response )
                                                                                            .getStdOut();
        tracker.onResponse( response, commandResult );

        verify( resourceHostEntity ).updateTemplateDownloadProgress( ENV_ID, "apache", 100 );
    }


    @Test
    public void testInstallingTemplate() throws Exception
    {
        doReturn( "time=\"2017-03-22 12:10:08\" level=info msg=\"Installing template cassandra\"" ).when( response )
                                                                                                   .getStdOut();
        tracker.onResponse( response, commandResult );

        verify( resourceHostEntity ).updateTemplateDownloadProgress( ENV_ID, "cassandra", 100 );
    }


    @Test
    public void testDownloadingTemplate() throws Exception
    {
        doReturn( "time=\"2017-03-22 12:10:08\" level=info msg=\"Downloading cassandra\" \n"
                + " 162.94 MiB / 209.08 MiB   77.93% 30s\n" + " 163.28 MiB / 209.08 MiB   78.10% 30s\n"
                + " 163.58 MiB / 209.08 MiB   78.24% 30s\n" + " 163.84 MiB / 209.08 MiB   78.36% 30s\n"
                + " 164.19 MiB / 209.08 MiB   78.53% 29s\n" + " 164.52 MiB / 209.08 MiB   78.69% 29s\n"
                + " 164.84 MiB / 209.08 MiB   78.84% 29s\n" + " 165.14 MiB / 209.08 MiB   78.99% 29s\n"
                + " 165.47 MiB / 209.08 MiB   79.14% 29s\n" + " 165.77 MiB / 209.08 MiB   79.28% 28s\n"
                + " 166.08 MiB / 209.08 MiB   79.43% 28s\n" + " 166.41 MiB / 209.08 MiB   79.59% 28s" )
                .when( commandResult ).getStdOut();
        tracker.onResponse( response, commandResult );

        verify( resourceHostEntity, atLeastOnce() ).updateTemplateDownloadProgress( ENV_ID, "cassandra", 78 );
        verify( resourceHostEntity, atLeastOnce() ).updateTemplateDownloadProgress( ENV_ID, "cassandra", 79 );
    }
}
