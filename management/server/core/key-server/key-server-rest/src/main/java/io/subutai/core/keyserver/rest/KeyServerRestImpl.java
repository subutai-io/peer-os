package io.subutai.core.keyserver.rest;


import java.util.List;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.keyserver.api.KeyServer;
import io.subutai.core.keyserver.api.model.SecurityKey;


/**
 * Implementation of Key Server Rest
 */
public class KeyServerRestImpl implements KeyServerRest
{

    private static final Logger LOG = LoggerFactory.getLogger( KeyServerRestImpl.class.getName() );

    private KeyServer keyServer;


    /********************************
     *
     */
    public KeyServerRestImpl(KeyServer keyServer)
    {
        this.keyServer = keyServer;
    }

    /********************************
     *
     */

    @Override
    public Response saveSecurityKey( String keyId,String fingerprint,short keyType,String keyData )
    {
        keyServer.saveSecurityKey( keyId, fingerprint,keyType, keyData.getBytes() );

        return Response.ok().build();
    }


    /********************************
     *
     */
    @Override
    public Response getSecurityKey( String keyId )
    {
        SecurityKey securityKey = keyServer.getSecurityKey( keyId );

        return Response.ok( JsonUtil.toJson( securityKey ) ).build();
    }


    /********************************
     *
     */
    @Override
    public Response getSecurityKeys()
    {
        List<SecurityKey> securityKeyList = keyServer.getSecurityKeyList();

        return Response.ok( JsonUtil.toJson( securityKeyList ) ).build();
    }


    /********************************
     *
     */
    @Override
    public Response removeSecurityKey( String keyId )
    {
        keyServer.removeSecurityKeyByKeyId( keyId );

        return Response.ok().build();
    }

    /********************************
     *
     */
    public KeyServer getKeyServer()
    {
        return keyServer;
    }


    /********************************
     *
     */
    public void setKeyServer( KeyServer keyServer )
    {
        this.keyServer = keyServer;
    }

}
