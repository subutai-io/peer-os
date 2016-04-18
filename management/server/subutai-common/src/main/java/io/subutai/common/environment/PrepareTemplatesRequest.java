package io.subutai.common.environment;


import java.util.Map;
import java.util.Set;


public class PrepareTemplatesRequest
{
    private final String environmentId;
    private Map<String, Set<String>> templates;


    public PrepareTemplatesRequest( final String environmentId, final Map<String, Set<String>> templates )
    {
        this.environmentId = environmentId;
        this.templates = templates;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public Map<String, Set<String>> getTemplates()
    {
        return templates;
    }
}
