package io.subutai.core.identity.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.dao.IdentityDataService;
import io.subutai.core.identity.impl.dao.IdentityDataServiceImpl;


/**
 * Implementation of Identity Manager
 */
public class IdentityManagerImpl implements IdentityManager
{
    private static final Logger LOG = LoggerFactory.getLogger( IdentityManagerImpl.class );

    private final DaoManager daoManager;
    private IdentityDataService identityDataService;


    /* *************************************************
     *
     */
    public IdentityManagerImpl( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /* *************************************************
     *
     */
    public void init()
    {
        LOG.info( "Initializing identity manager..." );

        identityDataService = new IdentityDataServiceImpl( daoManager );
    }
}
