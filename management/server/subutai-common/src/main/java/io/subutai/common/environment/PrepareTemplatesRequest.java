package io.subutai.common.environment;


import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;


public class PrepareTemplatesRequest
{
    @JsonProperty( value = "environmentId" )
    private final String environmentId;

    @JsonProperty( value = "kurjunToken" )
    private final String kurjunToken;

    @JsonProperty( value = "templates" )
    private Map<String, Set<String>> templates;


    public PrepareTemplatesRequest( @JsonProperty( value = "environmentId" ) final String environmentId,
                                    @JsonProperty( value = "kurjunToken" ) final String kurjunToken,
                                    @JsonProperty( value = "templates" ) final Map<String, Set<String>> templates )
    {
        this.environmentId = environmentId;
        this.templates = templates;
        this.kurjunToken = kurjunToken;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public String getKurjunToken()
    {
        return kurjunToken;
    }


    public Map<String, Set<String>> getTemplates()
    {
        return templates;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "PrepareTemplatesRequest{" );
        sb.append( "environmentId='" ).append( environmentId ).append( '\'' );
        sb.append( ", kurjunToken='" ).append( kurjunToken ).append( '\'' );
        sb.append( ", templates=" ).append( templates );
        sb.append( '}' );
        return sb.toString();
    }
}
