package org.safehaus.subutai.rest.template.manager;

import org.safehaus.subutai.api.template.manager.TemplateManager;

public class RestServiceImpl implements RestService {

    private TemplateManager templateManager;

    public TemplateManager getTemplateManager() {
        return templateManager;
    }

    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    @Override
    public String importTemplate() {
        return "some test";
    }

}
