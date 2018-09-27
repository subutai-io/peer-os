package io.subutai.core.bazaarmanager.impl.http;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.subutai.common.util.ExceptionUtil;
import io.subutai.common.util.TaskUtil;
import io.subutai.core.bazaarmanager.api.RestClient;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.impl.ConfigManager;
import io.subutai.bazaar.share.json.JsonUtil;


public class BazaarRestClient implements RestClient
{
    private static final String ERROR = "Error executing request to Bazaar: ";
    private static final int MAX_ATTEMPTS = 3;

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final ConfigManager configManager;


    public BazaarRestClient( ConfigManager configManager )
    {
        this.configManager = configManager;
    }


    @Override
    public <T> RestResult<T> get( String url, Class<T> clazz )
    {
        return execute( "GET", url, null, clazz, true );
    }


    @Override
    public <T> RestResult<T> getPlain( String url, Class<T> clazz )
    {
        return execute( "GET", url, null, clazz, false );
    }


    /**
     * Throws exception if result is not successful
     */
    @Override
    public <T> T getStrict( String url, Class<T> clazz ) throws BazaarManagerException
    {
        RestResult<T> restResult = get( url, clazz );

        if ( !restResult.isSuccess() )
        {
            throw new BazaarManagerException( restResult.getError() );
        }

        return restResult.getEntity();
    }


    @Override
    public <T> RestResult<T> post( String url, Object body, Class<T> clazz )
    {
        return execute( "POST", url, body, clazz, true );
    }


    @Override
    public RestResult<Object> post( String url, Object body )
    {
        return post( url, body, Object.class );
    }


    /**
     * Executes POST request without encrypting body and response
     */
    @Override
    public RestResult<Object> postPlain( String url, Object body )
    {
        return execute( "POST", url, body, Object.class, false );
    }


    @Override
    public <T> RestResult<T> put( String url, Object body, Class<T> clazz )
    {
        return execute( "PUT", url, body, clazz, true );
    }


    @Override
    public RestResult<Object> delete( String url )
    {
        return execute( "DELETE", url, null, Object.class, false );
    }


    private <T> RestResult<T> execute( String httpMethod, String url, Object body, Class<T> clazz, boolean encrypt )
    {
        log.info( "{} {}", httpMethod, url );

        WebClient webClient = null;
        Response response = null;
        RestResult<T> restResult = new RestResult<>( HttpStatus.SC_INTERNAL_SERVER_ERROR );

        try
        {
            webClient = configManager.getTrustedWebClientWithAuth( url, configManager.getbazaarIp() );

            Object requestBody = encrypt ? encryptBody( body ) : body;

            response = webClient.invoke( httpMethod, requestBody );

            // retry on 503 http code >>>
            int attemptNo = 1;
            while ( response.getStatus() == HttpStatus.SC_SERVICE_UNAVAILABLE && attemptNo < MAX_ATTEMPTS )
            {
                attemptNo++;
                response = webClient.invoke( httpMethod, requestBody );
                TaskUtil.sleep( 500 );
            }
            // <<< retry on 503 http code

            log.info( "response.status: {} - {}", response.getStatus(), response.getStatusInfo().getReasonPhrase() );

            restResult = handleResponse( response, clazz, encrypt );
        }
        catch ( Exception e )
        {
            if ( response != null )
            {
                restResult.setReasonPhrase( response.getStatusInfo().getReasonPhrase() );
            }

            Throwable rootCause = ExceptionUtil.getRootCauze( e );
            if ( rootCause instanceof ConnectException || rootCause instanceof UnknownHostException
                    || rootCause instanceof BindException || rootCause instanceof NoRouteToHostException
                    || rootCause instanceof PortUnreachableException || rootCause instanceof SocketTimeoutException )
            {
                restResult.setError( CONNECTION_EXCEPTION_MARKER );
            }
            else
            {
                restResult.setError( ERROR + e.getMessage() );
            }

            log.error( ERROR + e.getMessage() );
        }
        finally
        {
            close( webClient, response );
        }

        return restResult;
    }


    private void close( WebClient webClient, Response response )
    {
        if ( response != null )
        {
            try
            {
                response.close();
            }
            catch ( Exception ignore )
            {
            }
        }

        if ( webClient != null )
        {
            try
            {
                webClient.close();
            }
            catch ( Exception ignore )
            {
            }
        }
    }


    private byte[] encryptBody( Object body ) throws PGPException, JsonProcessingException
    {
        byte[] cborData = JsonUtil.toCbor( body );

        return configManager.getMessenger().produce( cborData );
    }


    private <T> RestResult<T> handleResponse( Response response, Class<T> clazz, boolean encrypt )
            throws IOException, PGPException
    {
        RestResult<T> restResult = new RestResult<>( response.getStatus() );

        restResult.setReasonPhrase( response.getStatusInfo().getReasonPhrase() );

        if ( !restResult.isSuccess() )
        {
            String error = response.readEntity( String.class );

            restResult.setError( error );

            log.error( "error: {}", StringUtils.abbreviate( error, 250 ) );

            return restResult;
        }

        if ( encrypt )
        {
            byte[] encryptedContent = readContent( response );

            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );

            restResult.setEntity( JsonUtil.fromCbor( plainContent, clazz ) );
        }
        else
        {
            restResult.setEntity( response.readEntity( clazz ) );
        }

        return restResult;
    }


    private byte[] readContent( Response response ) throws IOException
    {
        if ( response.getEntity() == null )
        {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        InputStream is = ( InputStream ) response.getEntity();

        IOUtils.copy( is, bos );

        return bos.toByteArray();
    }
}
