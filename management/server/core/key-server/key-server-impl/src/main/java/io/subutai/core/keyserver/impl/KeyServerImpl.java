package io.subutai.core.keyserver.impl;


import io.subutai.common.dao.DaoManager;
import io.subutai.core.keyserver.api.KeyServer;
import io.subutai.core.keyserver.api.dao.KeyServerDAO;
import io.subutai.core.keyserver.api.model.SecurityKey;
import io.subutai.core.keyserver.impl.dao.KeyServerDAOImpl;
import io.subutai.core.keyserver.impl.model.SecurityKeyEntity;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages Security key.
 */
public class KeyServerImpl implements KeyServer
{

    private static final Logger LOG = LoggerFactory.getLogger( KeyServerImpl.class );
    private DaoManager daoManager = null;
    private KeyServerDAO keyServerDAO = null;

    /********************************
     *
     */
    public void init()
    {
        keyServerDAO = new KeyServerDAOImpl( daoManager );
    }


    /********************************
     *
     */
    public void destroy()
    {

    }


    /********************************
     *
     */
    public DaoManager getDaoManager()
    {
        return daoManager;
    }


    /********************************
     *
     */
    public void setDaoManager( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /********************************
     *
     */
    @Override
    public SecurityKey getSecurityKeyByFingerprint( String fingerprint )
    {
        return keyServerDAO.findByFingerprint( fingerprint );
    }


    /********************************
     *
     */
    @Override
    public SecurityKey getSecurityKeyByKeyId( String keyId )
    {
        return keyServerDAO.findByKeyId( keyId );
    }


    /********************************
     *
     */
    @Override
    public SecurityKey getSecurityKey( String keyId )
    {
        return keyServerDAO.find( keyId );
    }


    /********************************
     *
     */
    @Override
    public List<SecurityKey> getSecurityKeyList()
    {
        return keyServerDAO.findAll();
    }


    /********************************
     *
     */
    @Override
    public void saveSecurityKey( SecurityKey securityKey )
    {
        keyServerDAO.save( securityKey );
    }

    /********************************
     *
     */
    @Override
    public void saveSecurityKey( String keyId,String fingerprint,short keyType,byte[] keyData )
    {
        SecurityKey securityKey = new SecurityKeyEntity();

        securityKey.setKeyId( keyId );
        securityKey.setFingerprint( fingerprint );
        securityKey.setKeyType( keyType );
        securityKey.setKeyData( keyData );

        keyServerDAO.save( securityKey );
    }


    /********************************
     *
     */
    @Override
    public void removeSecurityKey( SecurityKey securityKey )
    {
        keyServerDAO.delete( securityKey );
    }


    /********************************
     *
     */
    @Override
    public void removeSecurityKeyByKeyId( String keyId )
    {
        keyServerDAO.deleteByKeyId( keyId );
    }


    /********************************
     *
     */
    @Override
    public KeyServerDAO getKeyServerDAO()
    {
        return keyServerDAO;
    }


    /********************************
     *
     */
    @Override
    public void setKeyServerDAO( KeyServerDAO keyServerDAO )
    {
        this.keyServerDAO = keyServerDAO;
    }
}
