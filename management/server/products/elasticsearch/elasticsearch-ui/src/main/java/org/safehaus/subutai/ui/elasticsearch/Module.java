package org.safehaus.subutai.ui.elasticsearch;


import com.vaadin.ui.Component;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.elasticsearch.ElasticSearch;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.shared.protocol.FileUtil;

import java.io.File;


public class Module implements PortalModule {

    public static final String MODULE_IMAGE = "monitor.png";
    private static final String MODULE_NAME = "ElasticSearch";

    private AgentManager agentManager;
    private ElasticSearch elasticSearch;


    public void setAgentManager( AgentManager agentManager ) {
        this.agentManager = agentManager;
    }


    public void setElasticSearch( ElasticSearch elasticSearch ) {
        this.elasticSearch = elasticSearch;
    }


    @Override
    public String getId() {
        return MODULE_NAME;
    }


    @Override
    public String getName() {
        return MODULE_NAME;
    }


    @Override
    public File getImage() {
        return FileUtil.getFile( Module.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent() {
        return new View( agentManager, elasticSearch );
    }
}
