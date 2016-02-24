package io.subutai.common.environment;


import java.util.Map;
import java.util.Set;


public class PrepareTemplatesRequest
{
    private Map<String, Set<String>> templates;


    public PrepareTemplatesRequest( final Map<String, Set<String>> templates )
    {
        this.templates = templates;
    }


    public Map<String, Set<String>> getTemplates()
    {
        return templates;
    }
}
