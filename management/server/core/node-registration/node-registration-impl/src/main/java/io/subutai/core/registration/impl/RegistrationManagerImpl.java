package io.subutai.core.registration.impl;


import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.host.HostArchitecture;
import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.registration.api.RegistrationStatus;
import io.subutai.core.registration.api.resource.host.RequestedHost;
import io.subutai.core.registration.impl.resource.RequestDataService;
import io.subutai.core.registration.impl.resource.entity.RequestedHostImpl;


/**
 * Created by talas on 8/24/15.
 */
public class RegistrationManagerImpl implements RegistrationManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger( RegistrationManagerImpl.class );
    private RequestDataService requestDataService;


    public void init()
    {
        RequestedHostImpl temp =
                new RequestedHostImpl( UUID.randomUUID().toString(), "hostname", HostArchitecture.AMD64, "some key",
                        "some rest hook", RegistrationStatus.REQUESTED );
        requestDataService.persist( temp );
        LOGGER.error( "Started RegistrationManagerImpl" );
        List<RequestedHostImpl> requestedHosts = ( List<RequestedHostImpl> ) requestDataService.getAll();
        for ( final RequestedHostImpl requestedHost : requestedHosts )
        {
            LOGGER.error( requestedHost.toString() );
        }
    }


    public RequestDataService getRequestDataService()
    {
        return requestDataService;
    }


    public void setRequestDataService( final RequestDataService requestDataService )
    {
        this.requestDataService = requestDataService;
    }


    @Override
    public List<RequestedHost> getRequests()
    {
        List<RequestedHost> temp = Lists.newArrayList();
        temp.addAll( requestDataService.getAll() );
        return temp;
    }


    @Override
    public void queueRequest( final RequestedHost requestedHost )
    {
        requestDataService.persist( ( RequestedHostImpl ) requestedHost );
    }


    @Override
    public void rejectRequest( final UUID requestId )
    {
        RequestedHostImpl registrationRequest = requestDataService.find( requestId );
        registrationRequest.setStatus( RegistrationStatus.REJECTED );
    }


    @Override
    public void approveRequest( final UUID requestId )
    {
        RequestedHostImpl registrationRequest = requestDataService.find( requestId );
        registrationRequest.setStatus( RegistrationStatus.APPROVED );

        /**
         * TODO Perform key saving process in KeyServer
         */
    }
}
