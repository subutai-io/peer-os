package io.subutai.core.hubmanager.impl.proccessors;


import java.io.IOException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.core.appender.SubutaiErrorEvent;
import io.subutai.core.appender.SubutaiErrorEventListener;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.IntegrationImpl;
import io.subutai.hub.share.dto.SystemLogsDto;
import io.subutai.hub.share.json.JsonUtil;


public class HubLoggerProcessor implements Runnable, SubutaiErrorEventListener
{
    private static final Logger LOG = LoggerFactory.getLogger( HubLoggerProcessor.class.getName() );
    private ConfigManager configManager;
    private IntegrationImpl manager;
    private static Map<String, String> errLogs = new ConcurrentHashMap<>();


    public HubLoggerProcessor( final ConfigManager configManager, final IntegrationImpl integration )
    {
        this.configManager = configManager;
        this.manager = integration;
    }


    public HubLoggerProcessor()
    {
    }


    @Override
    public void run()
    {
        if ( !errLogs.isEmpty() && manager.getRegistrationState() )
        {
            try
            {
                List<String> valueList = new ArrayList<String>( errLogs.values() );
                Set<String> sendingLogs = new HashSet<>();

                if ( valueList.size() >= 5 )
                {
                    sendingLogs.add( valueList.get( valueList.size() - 1 ) );
                    sendingLogs.add( valueList.get( valueList.size() - 2 ) );
                    sendingLogs.add( valueList.get( valueList.size() - 3 ) );
                    sendingLogs.add( valueList.get( valueList.size() - 4 ) );
                    sendingLogs.add( valueList.get( valueList.size() - 5 ) );
                }

                WebClient client =
                        configManager.getTrustedWebClientWithAuth( "/rest/v1/system-bugs", configManager.getHubIp() );
                SystemLogsDto logsDto = new SystemLogsDto();
                logsDto.setLogs( sendingLogs );

                byte[] plainData = JsonUtil.toCbor( logsDto );
                byte[] encryptedData = configManager.getMessenger().produce( plainData );

                LOG.debug( "Sending System logs to HUB:" );

                Response r = client.post( encryptedData );

                if ( r.getStatus() != HttpStatus.SC_NO_CONTENT )
                {
                    LOG.error( "Could not send data to Hub (REST)", r.readEntity( String.class ) );
                }

                LOG.debug( "System logs sent to HUB successfully." );

                errLogs.clear();
            }
            catch ( PGPException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException |
                    IOException e )
            {
                LOG.error( "Could not send data to Hub (SS)", e.getMessage() );
            }
        }
    }


    @Override
    public void onEvent( final SubutaiErrorEvent event )
    {
        LOG.info( String.format( "RECEIVED:%n:%s", event.toString() ) );

        try
        {
            byte[] loggerName = event.getLoggerName().getBytes();
            byte[] renderedMsg = event.getRenderedMessage().getBytes();
            byte[] combined = ArrayUtils.addAll( loggerName, renderedMsg );
            MessageDigest md = MessageDigest.getInstance( "MD5" );
            byte[] theDigest = md.digest( combined );
            String key = new String( theDigest );

            errLogs.put( key, event.toString() );
        }
        catch ( Exception e )
        {
            LOG.error( "******* Error in collecting logs to Map object.", e );
        }
    }
}
