package io.subutai.core.hubmanager.impl.processor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.peer.ResourceHost;
import io.subutai.common.settings.SubutaiInfo;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.HubManagerImpl;
import io.subutai.core.hubmanager.impl.http.HubRestClient;
import io.subutai.core.hubmanager.impl.http.RestResult;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.VersionInfoDto;

import static java.lang.String.format;


public class VersionInfoProcessor implements Runnable
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private ConfigManager configManager;

    private HubManagerImpl manager;

    private PeerManager peerManager;

    private final HubRestClient restClient;


    public VersionInfoProcessor( final HubManagerImpl integration, final PeerManager peerManager,
                                 final ConfigManager configManager )
    {
        this.peerManager = peerManager;
        this.configManager = configManager;
        this.manager = integration;

        restClient = new HubRestClient( configManager );
    }


    @Override
    public void run()
    {
        try
        {
            if ( manager.isRegistered() )
            {
                sendVersionInfo();
            }
        }
        catch ( Exception e )
        {
            log.debug( "Sending version info failed." );

            log.error( e.getMessage(), e );
        }
    }


    public void sendVersionInfo() throws Exception
    {
        if ( manager.isRegistered() )
        {
            String path = format( "/rest/v1/peers/%s/version-info", peerManager.getLocalPeer().getId() );

            VersionInfoDto versionInfoDto = new VersionInfoDto();

            versionInfoDto.setPeerId( configManager.getPeerId() );
            versionInfoDto.setSsVersion( SubutaiInfo.getVersion() );
            versionInfoDto.setBuildTime( SubutaiInfo.getBuildTime() );
            versionInfoDto.setBranch( SubutaiInfo.getBranch() );
            versionInfoDto.setCommitId( SubutaiInfo.getCommitId() );

            ResourceHost host = configManager.getPeerManager().getLocalPeer().getManagementHost();

            versionInfoDto.setP2pVersion( host.getP2pVersion().replace( "p2p Cloud project", "" ).trim() );
            versionInfoDto.setRhVersion( host.getRhVersion().replace( "Subutai version", "" ).trim() );

            RestResult<Object> restResult = restClient.post( path, versionInfoDto );
            if ( !restResult.isSuccess() )
            {
                throw new Exception( "Error on sending version info to hub: " + restResult.getError() );
            }
        }
    }
}
