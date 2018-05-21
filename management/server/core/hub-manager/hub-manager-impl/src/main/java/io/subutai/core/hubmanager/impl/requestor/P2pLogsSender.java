package io.subutai.core.hubmanager.impl.requestor;


import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.time.DateUtils;

import com.google.common.collect.Lists;

import io.subutai.common.peer.LocalPeer;
import io.subutai.core.hubmanager.api.HubRequester;
import io.subutai.core.hubmanager.api.RestClient;
import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.HubManagerImpl;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.pojo.P2Pinfo;
import io.subutai.hub.share.dto.P2PDto;
import io.subutai.hub.share.dto.SystemLogsDto;

import static java.lang.String.format;


public class P2pLogsSender extends HubRequester
{

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private LocalPeer localPeer;

    private Monitor monitor;

    private Date p2pLogsEndDate;


    public P2pLogsSender( HubManagerImpl hubManager, LocalPeer localPeer, Monitor monitor,
                          RestClient restClient )
    {
        super( hubManager, restClient );

        this.localPeer = localPeer;

        this.monitor = monitor;
    }


    @Override
    public void request() throws HubManagerException
    {
        process();
    }


    public void process() throws HubManagerException
    {
        processP2PLogs();
    }


    private void processP2PLogs()
    {
        Date currentDate = new Date();

        Date startDate = p2pLogsEndDate != null ? p2pLogsEndDate : DateUtils.addMinutes( currentDate, -15 );

        p2pLogsEndDate = currentDate;

        try
        {
            processP2PLogs( startDate, currentDate );
        }
        catch ( Exception e )
        {
            log.error( "Error to process p2p logs: {} ", e.getMessage() );
        }
    }


    private void processP2PLogs( Date startDate, Date endDate ) throws HubManagerException
    {
        try
        {
            log.info( "Getting p2p logs: {} - {}", startDate, endDate );

            List<P2Pinfo> p2Pinfos = monitor.getP2PStatus( startDate, endDate );

            List<P2PDto> p2pList = Lists.newArrayList();

            for ( final P2Pinfo info : p2Pinfos )
            {
                P2PDto dto = new P2PDto();
                dto.setRhId( info.getRhId() );
                dto.setRhVersion( info.getRhVersion() );
                dto.setP2pVersion( info.getP2pVersion() );
                dto.setP2pStatus( info.getP2pStatus() );
                dto.setState( info.getState() );

                p2pList.add( dto );
            }

            SystemLogsDto logsDto = new SystemLogsDto();
            logsDto.setP2PInfo( p2pList );

            log.info( "Sending p2p logs and status to Hub..." );

            String path = format( "/rest/v1/peers/%s/resource-hosts/system-logs", localPeer.getId() );

            RestResult<Object> restResult = restClient.post( path, logsDto );

            if ( restResult.isSuccess() )
            {
                log.info( "Processing p2p logs completed successfully" );
            }
        }
        catch ( Exception e )
        {
            throw new HubManagerException( e );
        }
    }
}
