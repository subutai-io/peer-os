package io.subutai.core.template.rest;


import javax.ws.rs.core.Response;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import io.subutai.common.gson.required.RequiredDeserializer;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.template.api.TemplateManager;


public class RestServiceImpl implements RestService
{
    private final TemplateManager templateManager;
    private final IdentityManager identityManager;
    private Gson gson = RequiredDeserializer.createValidatingGson();


    public RestServiceImpl( final TemplateManager templateManager, final IdentityManager identityManager )
    {
        Preconditions.checkNotNull( templateManager );
        Preconditions.checkNotNull( identityManager );

        this.templateManager = templateManager;
        this.identityManager = identityManager;
    }


    @Override
    public Response listTemplates()
    {
        return Response.ok().entity( gson.toJson( templateManager.getTemplates() ) ).build();
    }


    @Override
    public Response getFingerprint()
    {
        return Response.ok().entity( templateManager.getFingerprint() ).build();
    }


    @Override
    public Response getObtainedCdnToken()
    {
        return Response.ok().entity( templateManager.getObtainedCdnToken() ).build();
    }


    @Override
    public Response obtainCdnToken( final String signedFingerprint )
    {
        return Response.ok().entity( templateManager.obtainCdnToken( signedFingerprint ) ).build();
    }


    @Override
    public Response isRegisteredWithCdn()
    {
        return Response.ok().entity( templateManager.isRegisteredWithCdn() ).build();
    }
}
