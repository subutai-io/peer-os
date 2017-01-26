package io.subutai.common.environment;


import java.util.Map;
import java.util.Set;


public class PrepareTemplatesRequest
{
    private final String environmentId;
    private final String token;
    private Map<String, Set<String>> templates;


    public PrepareTemplatesRequest( final String environmentId, final String token,
                                    final Map<String, Set<String>> templates )
    {
        this.environmentId = environmentId;
        this.templates = templates;
        this.token = token;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public String getToken()
    {
        return token;
    }


    public Map<String, Set<String>> getTemplates()
    {
        return templates;
    }
}
