package io.subutai.core.bazaarmanager.impl.requestor;


import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.peer.LocalPeer;
import io.subutai.core.bazaarmanager.api.BazaarRequester;
import io.subutai.core.bazaarmanager.api.RestClient;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.impl.BazaarManagerImpl;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.pojo.P2PInfo;
import io.subutai.bazaar.share.dto.P2PDto;
import io.subutai.bazaar.share.dto.SystemLogsDto;

import static java.lang.String.format;


public class P2pStatusSender extends BazaarRequester
{

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private LocalPeer localPeer;
    private Monitor monitor;
    private Date lastSendDate;


    public P2pStatusSender( BazaarManagerImpl bazaarmanager, LocalPeer localPeer, Monitor monitor, RestClient restClient )
    {
        super( bazaarmanager, restClient );

        this.localPeer = localPeer;

        this.monitor = monitor;
    }


    @Override
    public void request()
    {
        try
        {
            sendP2PStatus();
        }
        catch ( Exception e )
        {
            log.error( "Error sending P2P status: {} ", e.getMessage() );
        }
    }


    private void sendP2PStatus() throws BazaarManagerException
    {
        try
        {
            log.info( "Getting P2P status" );

            List<P2PInfo> p2PInfos = monitor.getP2PStatuses();

            List<P2PDto> p2pList = Lists.newArrayList();

            boolean hasProblems = false;
            for ( final P2PInfo info : p2PInfos )
            {
                P2PDto dto = new P2PDto();
                dto.setRhId( info.getRhId() );
                dto.setP2pStatus( info.getP2pStatus() );
                dto.setState( info.getState() );

                if ( info.getP2pStatus() != 0 )
                {
                    hasProblems = true;
                }

                p2pList.add( dto );
            }

            if ( !hasProblems && lastSendDate != null
                    && System.currentTimeMillis() - lastSendDate.getTime() < TimeUnit.MINUTES.toMillis( 60 ) )
            {
                return;
            }


            SystemLogsDto logsDto = new SystemLogsDto();
            logsDto.setP2PInfo( p2pList );

            log.info( "Sending P2P status to Bazaar..." );

            String path = format( "/rest/v1/peers/%s/resource-hosts/system-logs", localPeer.getId() );

            RestResult<Object> restResult = restClient.post( path, logsDto );

            if ( restResult.isSuccess() )
            {
                log.info( "P2P status was sent to Bazaar successfully" );

                lastSendDate = new Date();
            }
        }
        catch ( Exception e )
        {
            throw new BazaarManagerException( e );
        }
    }
}
